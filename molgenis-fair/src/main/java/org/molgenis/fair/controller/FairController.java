package org.molgenis.fair.controller;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.rdf.TripleStore;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE_VALUE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Serves metadata for the molgenis FAIR DataPoint.
 */
@Controller
@RequestMapping(BASE_URI)
public class FairController
{
	private static final Logger LOG = LoggerFactory.getLogger(FairController.class);

	static final String BASE_URI = "/fdp";

	private final DataService dataService;
	private final EntityModelWriter entityModelWriter;
	private final TripleStore tripleStore;
	private static final String VOID = "http://rdfs.org/ns/void#";
	private static final String HYDRA = "http://www.w3.org/ns/hydra/core#";
	private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public FairController(DataService dataService, EntityModelWriter entityModelWriter, TripleStore tripleStore)
	{
		this.dataService = requireNonNull(dataService);
		this.entityModelWriter = requireNonNull(entityModelWriter);
		this.tripleStore = requireNonNull(tripleStore);
	}

	private static UriComponentsBuilder getBaseUri()
	{
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(BASE_URI);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, path = "/fdp.ttl")
	@ResponseBody
	@RunAsSystem
	public Model getMetadata()
	{
		Entity subjectEntity = dataService.findOne("fdp_Metadata", new QueryImpl<>());
		return entityModelWriter.createRdfModel(getBaseUri().pathSegment("fdp").toUriString(), subjectEntity);
	}

	@PostMapping(path = "/exportEntityType")
	public ResponseEntity<String> exportEntityType(@RequestParam String dataset, @RequestParam String entityTypeId)
	{
		Model model = entityModelWriter.createEmptyModel();
		Stream<Entity> entities = dataService.findAll(entityTypeId);
		entities.forEach(entity ->
		{
			String subjectIRI = createIri(entity);
			entityModelWriter.addEntityToModel(subjectIRI, entity, model);
		});
		tripleStore.store(dataset, model);
		UriComponents uri = getBaseUri().pathSegment("fragments", dataset).build();
		return ResponseEntity.created(uri.toUri()).body(uri.toUriString());
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/{catalogID}")
	@ResponseBody
	@RunAsSystem
	public Model getCatalog(@PathVariable("catalogID") String catalogID)
	{
		String subjectIRI = getBaseUri().pathSegment(catalogID).toUriString();
		Entity subjectEntity = dataService.findOneById("fdp_Catalog", catalogID);
		if (subjectEntity == null)
		{
			throw new UnknownEntityException(format("Catalog with id [%s] does not exist", catalogID));
		}
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/{catalogID}/{datasetID}")
	@ResponseBody
	@RunAsSystem
	public Model getDataset(@PathVariable("catalogID") String catalogID, @PathVariable("datasetID") String datasetID)
	{
		String subjectIRI = getBaseUri().pathSegment(catalogID, datasetID).toUriString();
		Entity subjectEntity = dataService.findOneById("fdp_Dataset", datasetID);
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/{catalogID}/{datasetID}/{distributionID}")
	@ResponseBody
	@RunAsSystem
	public Model getDistribution(@PathVariable("catalogID") String catalogID,
			@PathVariable("datasetID") String datasetID, @PathVariable("distributionID") String distributionID)
	{
		String subjectIRI = getBaseUri().pathSegment(catalogID, datasetID, distributionID).toUriString();
		Entity subjectEntity = dataService.findOneById("fdp_Distribution", distributionID);
		return entityModelWriter.createRdfModel(subjectIRI, subjectEntity);
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/fragments/{dataset}")
	@ResponseBody
	@RunAsSystem
	public Model getData(@PathVariable String dataset, @RequestParam(value = "s", required = false) String subject,
			@RequestParam(value = "p", required = false) String predicate,
			@RequestParam(value = "o", required = false) String object)
	{
		//TODO: add paging
		LOG.debug("dataset=[{}], s=[{}], p=[{}], o=[{}]", dataset, subject, predicate, object);

		Model result = tripleStore.findAll(dataset, subject, predicate, object);
		addMetadata(result);
		return result;
	}

	/**
	 * Adds metadata to the result.
	 * <p>
	 * Metadata should look like
	 * <code><http://example.org/example#dataset>
	 * void:subset <http://example.org/example?s=http%3A%2F%2Fexample.org%2Ftopic>;
	 * hydra:search [
	 * hydra:template "http://example.org/example{?s,p,o}";
	 * hydra:mapping  [ hydra:variable "s"; hydra:property rdf:subject ],
	 * [ hydra:variable "p"; hydra:property rdf:predicate ],
	 * [ hydra:variable "o"; hydra:property rdf:object ]
	 * ].</code
	 * >
	 *
	 * @param result the Model containing the fragment data
	 * @link http://www.hydra-cg.com/spec/latest/triple-pattern-fragments/
	 */
	private void addMetadata(Model result)
	{
		result.setNamespace("void", VOID);
		result.setNamespace("hydra", HYDRA);
		result.setNamespace("rdf", RDF);

		SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
		Resource fragmentIri = valueFactory.createIRI(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
		addCountStatements(result, fragmentIri, result.size());

		// turn into a linked data fragment
		String datasetUrl = ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString();
		Resource datasetIri = valueFactory.createIRI(datasetUrl);
		result.add(datasetIri, valueFactory.createIRI(VOID, "subset"), fragmentIri);

		BNode searchNode = valueFactory.createBNode();
		result.add(datasetIri, valueFactory.createIRI(HYDRA, "search"), searchNode);
		result.add(searchNode, valueFactory.createIRI(HYDRA, "template"),
				valueFactory.createLiteral(datasetUrl + "{?s,p,o}"));

		describeUrlParameter("s", valueFactory.createIRI(RDF, "subject"), result, searchNode);
		describeUrlParameter("p", valueFactory.createIRI(RDF, "predicate"), result, searchNode);
		describeUrlParameter("o", valueFactory.createIRI(RDF, "object"), result, searchNode);
	}

	private void describeUrlParameter(String parameterName, IRI parameterRole, Model result, BNode searchNode)
	{
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		BNode objectMapping = valueFactory.createBNode();
		result.add(searchNode, valueFactory.createIRI(HYDRA, "mapping"), objectMapping);
		result.add(objectMapping, valueFactory.createIRI(HYDRA, "variable"), valueFactory.createLiteral(parameterName));
		result.add(objectMapping, valueFactory.createIRI(HYDRA, "property"), parameterRole);
	}

	private void addCountStatements(Model data, Resource fragmentIri, int itemCount)
	{
		//TODO: update when we add paging
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		IRI triples = valueFactory.createIRI(VOID, "triples");
		IRI hydraTotalCount = valueFactory.createIRI(HYDRA, "totalItems");
		Value count = valueFactory.createLiteral(itemCount);
		data.add(fragmentIri, triples, count);
		data.add(fragmentIri, hydraTotalCount, count);
	}

	public String createIri(Entity entity)
	{
		return getBaseUri().pathSegment("resource", entity.getEntityType().getId(), entity.getIdValue().toString())
						   .toUriString();
	}

	@GetMapping(produces = TEXT_TURTLE_VALUE, value = "/resource/{entityTypeId}/{id}")
	@ResponseBody
	@RunAsSystem
	public Model getEntityData(@PathVariable("entityTypeId") String entityTypeId, @PathVariable("id") String entityId)
	{
		Model model = entityModelWriter.createEmptyModel();
		Entity entity = dataService.findOneById(entityTypeId, entityId);
		if (entity == null)
		{
			throw new UnknownEntityException();
		}
		String subjectIRI = createIri(entity);
		entityModelWriter.addEntityToModel(subjectIRI, entity, model);
		return model;
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	public Model handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.warn(e.getMessage(), e);
		return new LinkedHashModel();
	}
}
