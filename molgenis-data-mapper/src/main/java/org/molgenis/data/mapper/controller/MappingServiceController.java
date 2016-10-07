package org.molgenis.data.mapper.controller;

import com.google.common.collect.*;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.*;
import org.molgenis.data.importer.ImportWizardController;
import org.molgenis.data.jobs.model.JobExecution.Status;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.mapper.data.request.GenerateAlgorithmRequest;
import org.molgenis.data.mapper.data.request.MappingServiceRequest;
import org.molgenis.data.mapper.jobs.mappingservice.MappingServiceJob;
import org.molgenis.data.mapper.jobs.mappingservice.MappingServiceJobExecution;
import org.molgenis.data.mapper.jobs.mappingservice.MappingServiceJobFactory;
import org.molgenis.data.mapper.jobs.mappingservice.meta.MappingServiceJobExecutionMetaData;
import org.molgenis.data.mapper.mapping.model.*;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.mapper.meta.MappingProjectMetaData;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.mapper.service.impl.AlgorithmEvaluation;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.EntityMetaDataUtils;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.ontology.core.model.OntologyTagObject;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.molgenis.data.mapper.controller.MappingServiceController.URI;
import static org.molgenis.data.mapper.jobs.mappingservice.meta.MappingServiceJobExecutionMetaData.MAPPING_PROJECT;
import static org.molgenis.data.mapper.jobs.mappingservice.meta.MappingServiceJobExecutionMetaData.MAPPING_SERVICE_JOB_EXECUTION;
import static org.molgenis.data.mapper.mapping.model.CategoryMapping.create;
import static org.molgenis.data.mapper.mapping.model.CategoryMapping.createEmpty;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(URI)
public class MappingServiceController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	public static final String ID = "mappingservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_MAPPING_PROJECTS = "view-mapping-projects";
	private static final String VIEW_ATTRIBUTE_MAPPING = "view-attribute-mapping";
	private static final String VIEW_SINGLE_MAPPING_PROJECT = "view-single-mapping-project";
	private static final String VIEW_CATEGORY_MAPPING_EDITOR = "view-advanced-mapping-editor";
	private static final String VIEW_ATTRIBUTE_MAPPING_FEEDBACK = "view-attribute-mapping-feedback";

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTagService ontologyTagService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	@Autowired
	private MenuReaderService menuReaderService;

	@Autowired
	private MappingServiceJobFactory mappingServiceJobFactory;

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private ExecutorService taskExecutor;

	@Autowired
	private MappingServiceJobExecutionMetaData mappingServiceJobExecutionMetaData;

	public MappingServiceController()
	{
		super(URI);
	}

	/**
	 * Initializes the model with all mapping projects and all entities to the model.
	 *
	 * @param model the model to initialized
	 * @return view name of the mapping projects list
	 */
	@RequestMapping
	public String viewMappingProjects(Model model)
	{
		model.addAttribute("mappingProjects", mappingService.getAllMappingProjects());
		model.addAttribute("entityMetaDatas", getWritableEntityMetaDatas());
		model.addAttribute("user", getCurrentUsername());
		model.addAttribute("admin", currentUserIsSu());
		model.addAttribute("importerUri", menuReaderService.getMenu().findMenuItemPath(ImportWizardController.ID));
		return VIEW_MAPPING_PROJECTS;
	}

	/**
	 * Adds a new mapping project.
	 *
	 * @param name         name of the mapping project
	 * @param targetEntity name of the project's first {@link MappingTarget}'s target entity
	 * @return redirect URL for the newly created mapping project
	 */
	@RequestMapping(value = "/addMappingProject", method = RequestMethod.POST)
	public String addMappingProject(@RequestParam("mapping-project-name") String name,
			@RequestParam("target-entity") String targetEntity)
	{
		mappingService.addMappingProject(name, getCurrentUser(), targetEntity);
		return "redirect:" + getMappingServiceMenuUrl();
	}

	/**
	 * Removes a mapping project
	 *
	 * @param mappingProjectId the ID of the mapping project
	 * @return redirect url to the same page to force a refresh
	 */
	@RequestMapping(value = "/removeMappingProject", method = RequestMethod.POST)
	public String deleteMappingProject(@RequestParam(required = true) String mappingProjectId)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(project))
		{
			LOG.info("Deleting mappingProject " + project.getName());
			mappingService.deleteMappingProject(mappingProjectId);
		}
		return "redirect:" + getMappingServiceMenuUrl();
	}

	/**
	 * Removes a attribute mapping
	 *
	 * @param mappingProjectId the ID of the mapping project
	 * @param target           the target entity
	 * @param source           the source entity
	 * @param attribute        the attribute that is mapped
	 * @return
	 */
	@RequestMapping(value = "/removeAttributeMapping", method = RequestMethod.POST)
	public String removeAttributeMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String attribute)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(project))
		{
			project.getMappingTarget(target).getMappingForSource(source).deleteAttributeMapping(attribute);
			mappingService.updateMappingEntity(project.getMappingTarget(target).getMappingForSource(source));
		}
		return "redirect:" + getMappingServiceMenuUrl() + "/mappingproject/" + project.getIdentifier();
	}

	/**
	 * Adds a new {@link EntityMapping} to an existing {@link MappingTarget}
	 *
	 * @param target           the name of the {@link MappingTarget}'s entity to add a source entity to
	 * @param source           the name of the source entity of the newly added {@link EntityMapping}
	 * @param mappingProjectId the ID of the {@link MappingTarget}'s {@link MappingProject}
	 * @return redirect URL for the mapping project
	 */
	@RequestMapping(value = "/addEntityMapping", method = RequestMethod.POST)
	public String addEntityMapping(@RequestParam String mappingProjectId, String target, String source)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);

		if (hasWritePermission(mappingProject))
		{
			MappingServiceJobExecution mappingServiceJobExecution = new MappingServiceJobExecution(
					mappingServiceJobExecutionMetaData, mappingService);

			mappingProject.getMappingTarget(target).addSource(dataService.getEntityMetaData(source));

			mappingService.updateMappingProject(mappingProject);

			Entity mappingProjectEntity = dataService
					.findOneById(MappingProjectMetaData.MAPPING_PROJECT, mappingProject.getIdentifier());

			EntityMetaData targetEntityMetaDataEntity = dataService.findOne(EntityMetaDataMetaData.ENTITY_META_DATA,
					new QueryImpl<EntityMetaData>().eq(EntityMetaDataMetaData.FULL_NAME, target), EntityMetaData.class);

			EntityMetaData sourceEntityMetaDataEntity = dataService.findOne(EntityMetaDataMetaData.ENTITY_META_DATA,
					new QueryImpl<EntityMetaData>().eq(EntityMetaDataMetaData.FULL_NAME, source), EntityMetaData.class);

			mappingServiceJobExecution.setMappingProject(mappingProjectEntity);
			mappingServiceJobExecution.setTargetEntity(targetEntityMetaDataEntity);
			mappingServiceJobExecution.setSourceEntity(Arrays.asList(sourceEntityMetaDataEntity));
			mappingServiceJobExecution.setUser(userAccountService.getCurrentUser());
			mappingServiceJobExecution.setResultUrl(getMappingServiceMenuUrl() + "/mappingproject/" + mappingProjectId);

			MappingServiceJob mappingServiceJob = mappingServiceJobFactory
					.create(mappingServiceJobExecution, SecurityContextHolder.getContext().getAuthentication());

			taskExecutor.submit(mappingServiceJob);
		}

		return "redirect:" + getMappingServiceMenuUrl();
	}

	/**
	 * Removes entity mapping
	 *
	 * @param mappingProjectId ID of the mapping project to remove entity mapping from
	 * @param target           entity name of the mapping target
	 * @param source           entity name of the mapping source
	 * @return redirect url of the mapping project's page
	 */
	@RequestMapping(value = "/removeEntityMapping", method = RequestMethod.POST)
	public String removeEntityMapping(@RequestParam String mappingProjectId, String target, String source)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(mappingProject))
		{
			MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
			EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
			mappingService.removeEntityMapping(mappingTarget, entityMapping);
		}
		return "redirect:" + getMappingServiceMenuUrl() + "/mappingproject/" + mappingProjectId;
	}

	@RequestMapping(value = "/validateAttrMapping", method = RequestMethod.POST)
	@ResponseBody
	public AttributeMappingValidationReport validateAttributeMapping(
			@Valid @RequestBody MappingServiceRequest mappingServiceRequest)
	{
		String targetEntityName = mappingServiceRequest.getTargetEntityName();
		EntityMetaData targetEntityMeta = dataService.getEntityMetaData(targetEntityName);

		String targetAttributeName = mappingServiceRequest.getTargetAttributeName();
		AttributeMetaData targetAttr = targetEntityMeta.getAttribute(targetAttributeName);
		if (targetAttr == null)
		{
			throw new UnknownAttributeException("Unknown attribute [" + targetAttributeName + "]");
		}

		String algorithm = mappingServiceRequest.getAlgorithm();
		Long offset = mappingServiceRequest.getOffset();
		Long num = mappingServiceRequest.getNum();
		Query<Entity> query = new QueryImpl<Entity>().offset(offset.intValue()).pageSize(num.intValue());
		String sourceEntityName = mappingServiceRequest.getSourceEntityName();
		Iterable<Entity> sourceEntities = new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return dataService.findAll(sourceEntityName, query).iterator();
			}
		};

		long total = dataService.count(sourceEntityName, new QueryImpl<>());
		long nrSuccess = 0, nrErrors = 0;
		Map<String, String> errorMessages = new LinkedHashMap<String, String>();
		for (AlgorithmEvaluation evaluation : algorithmService.applyAlgorithm(targetAttr, algorithm, sourceEntities))
		{
			if (evaluation.hasError())
			{
				errorMessages.put(evaluation.getEntity().getIdValue().toString(), evaluation.getErrorMessage());
				++nrErrors;
			}
			else
			{
				++nrSuccess;
			}
		}

		return new AttributeMappingValidationReport(total, nrSuccess, nrErrors, errorMessages);
	}

	private static class AttributeMappingValidationReport
	{
		private final Long total;
		private final Long nrSuccess;
		private final Long nrErrors;
		private final Map<String, String> errorMessages;

		public AttributeMappingValidationReport(Long total, Long nrSuccess, Long nrErrors,
				Map<String, String> errorMessages)
		{
			this.total = total;
			this.nrSuccess = nrSuccess;
			this.nrErrors = nrErrors;
			this.errorMessages = errorMessages;
		}

		@SuppressWarnings("unused")
		public Long getTotal()
		{
			return total;
		}

		@SuppressWarnings("unused")
		public Long getNrSuccess()
		{
			return nrSuccess;
		}

		@SuppressWarnings("unused")
		public Long getNrErrors()
		{
			return nrErrors;
		}

		@SuppressWarnings("unused")
		public Map<String, String> getErrorMessages()
		{
			return errorMessages;
		}
	}

	/**
	 * Adds a new {@link AttributeMapping} to an {@link EntityMapping}.
	 *
	 * @param mappingProjectId ID of the mapping project
	 * @param target           name of the target entity
	 * @param source           name of the source entity
	 * @param targetAttribute  name of the target attribute
	 * @param algorithm        the mapping algorithm
	 * @return redirect URL for the attributemapping
	 */
	@RequestMapping(value = "/saveattributemapping", method = RequestMethod.POST)
	public String saveAttributeMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute, @RequestParam(required = true) String algorithm,
			@RequestParam(required = true) AlgorithmState algorithmState)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(mappingProject))
		{
			MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
			EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
			if (algorithm.isEmpty())
			{
				entityMapping.deleteAttributeMapping(targetAttribute);
				mappingService.updateMappingEntity(entityMapping);
			}
			else
			{
				AttributeMapping attributeMapping = entityMapping.getAttributeMapping(targetAttribute);
				if (attributeMapping == null)
				{
					attributeMapping = entityMapping.addAttributeMapping(targetAttribute);
				}
				attributeMapping.setAlgorithm(algorithm);
				attributeMapping.setAlgorithmState(algorithmState);
				mappingService.updateAttributeMapping(attributeMapping);
			}
		}
		return "redirect:" + getMappingServiceMenuUrl() + "/mappingproject/" + mappingProject.getIdentifier();
	}

	/**
	 * Find the firstattributeMapping skip the the algorithmStates that are given in the {@link AttributeMapping} to an
	 * {@link EntityMapping}.
	 * <p>
	 *
	 * @param mappingProjectId    ID of the mapping project
	 * @param target              name of the target entity
	 * @param skipAlgorithmStates the mapping algorithm states that should skip
	 */
	@RequestMapping(value = "/firstattributemapping", method = RequestMethod.POST)
	@ResponseBody
	public FirstAttributeMappingInfo getFirstAttributeMappingInfo(
			@RequestParam(required = true) String mappingProjectId, @RequestParam(required = true) String target,
			@RequestParam(required = true, value = "skipAlgorithmStates[]") List<AlgorithmState> skipAlgorithmStates,
			Model model)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(mappingProject))
		{
			MappingTarget mappingTarget = mappingProject.getMappingTarget(target);

			List<String> sourceInvolvedInRunningJobs = getRunningJobs(mappingProjectId).stream()
					.map(MappingServiceJobExecution::getSourceEntities).flatMap(List::stream)
					.map(EntityMetaData::getName).collect(Collectors.toList());

			List<String> sourceNames = mappingTarget.getEntityMappings().stream()
					.map(i -> i.getSourceEntityMetaData().getName())
					.filter(source -> !sourceInvolvedInRunningJobs.contains(source)).collect(Collectors.toList());

			EntityMetaData targetEntityMeta = mappingTarget.getTarget();
			for (AttributeMetaData attributeMetaData : targetEntityMeta.getAtomicAttributes())
			{
				if (attributeMetaData.equals(targetEntityMeta.getIdAttribute()))
				{
					continue;
				}

				for (String source : sourceNames)
				{
					EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
					AttributeMapping attributeMapping = entityMapping.getAttributeMapping(attributeMetaData.getName());

					if (nonNull(attributeMapping))
					{
						AlgorithmState algorithmState = attributeMapping.getAlgorithmState();
						if (nonNull(skipAlgorithmStates))
						{
							if (skipAlgorithmStates.contains(algorithmState))
							{
								continue;
							}
						}
					}

					return FirstAttributeMappingInfo
							.create(mappingProjectId, target, source, attributeMetaData.getName());
				}
			}
		}

		return null;
	}

	private List<MappingServiceJobExecution> getRunningJobs(String mappingProjectIdentifier)
	{
		Stream<MappingServiceJobExecution> mappingServiceJobs = dataService.findAll(MAPPING_SERVICE_JOB_EXECUTION,
				new QueryImpl<MappingServiceJobExecution>().eq(MAPPING_PROJECT, mappingProjectIdentifier).and()
						.eq(JobExecutionMetaData.STATUS, Status.RUNNING.toString().toUpperCase()),
				MappingServiceJobExecution.class);

		return mappingServiceJobs.collect(Collectors.toList());
	}

	/**
	 * Displays a mapping project.
	 *
	 * @param identifier identifier of the {@link MappingProject}
	 * @param target     Name of the selected {@link MappingTarget}'s target entity
	 * @param model      the model
	 * @return View name of the
	 */
	@RequestMapping("/mappingproject/{id}")
	public String viewMappingProject(@PathVariable("id") String identifier,
			@RequestParam(value = "target", required = false) String target, Model model)
	{
		MappingProject project = mappingService.getMappingProject(identifier);
		if (target == null)
		{
			target = project.getMappingTargets().get(0).getName();
		}

		model.addAttribute("selectedTarget", target);
		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMetaDatas", getNewSources(project.getMappingTarget(target)));
		model.addAttribute("hasWritePermission", hasWritePermission(project, false));
		model.addAttribute("attributeTagMap", getTagsForAttribute(target, project));

		return VIEW_SINGLE_MAPPING_PROJECT;
	}

	@RequestMapping(value = "/mappingproject/clone", method = RequestMethod.POST)
	public String cloneMappingProject(@RequestParam("mappingProjectId") String mappingProjectId)
	{
		mappingService.cloneMappingProject(mappingProjectId);
		return "forward:" + URI;
	}

	/**
	 * This controller will first of all check if the user-defined search terms exist. If so, the searchTerms will be
	 * used directly in the SemanticSearchService. If the searchTerms are not defined by users, it will use the
	 * ontologyTermTags in the SemantiSearchService. If neither of the searchTerms and the OntologyTermTags exist, it
	 * will use the information from the targetAttribute in the SemanticSearchService
	 * <p>
	 * If string terms are sent to the SemanticSearchService, they will be first of all converted to the ontologyTerms
	 * using findTag method
	 *
	 * @param requestBody
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/attributeMapping/semanticsearch", consumes = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<ExplainedMatchCandidate<AttributeMetaData>> getSemanticSearchAttributeMapping(
			@RequestBody Map<String, String> requestBody)
	{
		String mappingProjectId = requestBody.get("mappingProjectId");
		String target = requestBody.get("target");
		String source = requestBody.get("source");
		String targetAttribute = requestBody.get("targetAttribute");
		String searchTermsString = requestBody.get("searchTerms");
		Set<String> userQueries = new LinkedHashSet<String>();

		if (StringUtils.isNotBlank(searchTermsString))
		{
			userQueries.addAll(of(searchTermsString.toLowerCase().split("\\s+or\\s+")).filter(StringUtils::isNotBlank)
					.map(String::trim).collect(toList()));
		}

		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);

		EntityMetaData targetEntityMetaData = entityMapping.getTargetEntityMetaData();
		EntityMetaData sourceEntityMetaData = entityMapping.getSourceEntityMetaData();
		AttributeMetaData targetAttributeMetaData = targetEntityMetaData.getAttribute(targetAttribute);

		// Find relevant attributes base on tags
		Multimap<Relation, OntologyTagObject> tagsForAttribute = ontologyTagService
				.getTagsForAttribute(entityMapping.getTargetEntityMetaData(), targetAttributeMetaData);

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> decisionTreeToFindRelevantAttributes = semanticSearchService
				.decisionTreeToFindRelevantAttributes(sourceEntityMetaData, targetAttributeMetaData,
						tagsForAttribute.values(), userQueries);

		return Lists.newArrayList(decisionTreeToFindRelevantAttributes.values());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/attributemapping/algorithm", consumes = APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getSuggestedAlgorithm(@RequestBody GenerateAlgorithmRequest generateAlgorithmRequest)
	{
		EntityMetaData targetEntityMetaData = dataService
				.getEntityMetaData(generateAlgorithmRequest.getTargetEntityName());

		EntityMetaData sourceEntityMetaData = dataService
				.getEntityMetaData(generateAlgorithmRequest.getSourceEntityName());

		AttributeMetaData targetAttribute = targetEntityMetaData
				.getAttribute(generateAlgorithmRequest.getTargetAttributeName());

		List<AttributeMetaData> sourceAttributes = generateAlgorithmRequest.getSourceAttributes().stream()
				.map(name -> sourceEntityMetaData.getAttribute(name)).collect(Collectors.toList());

		String generateAlgorithm = algorithmService
				.generateAlgorithm(targetAttribute, targetEntityMetaData, sourceAttributes, sourceEntityMetaData);

		return generateAlgorithm;
	}

	/**
	 * Creates the integrated entity for a mapping project's target
	 *
	 * @param mappingProjectId ID of the mapping project
	 * @param target           name of the target of the {@link EntityMapping}
	 * @param newEntityName    name of the new entity to create
	 * @return redirect URL to the data explorer displaying the newly generated entity
	 */
	@RequestMapping("/createIntegratedEntity")
	public String createIntegratedEntity(@RequestParam String mappingProjectId, @RequestParam String target,
			@RequestParam() String newEntityName, Model model)
	{
		try
		{
			MappingTarget mappingTarget = mappingService.getMappingProject(mappingProjectId).getMappingTarget(target);
			String name = mappingService.applyMappings(mappingTarget, newEntityName);
			return "redirect:" + menuReaderService.getMenu().findMenuItemPath(DataExplorerController.ID) + "?entity="
					+ name;
		}
		catch (RuntimeException ex)
		{
			model.addAttribute("heading", "Failed to create integrated entity.");
			model.addAttribute("message", ex.getMessage());
			model.addAttribute("href", getMappingServiceMenuUrl() + "/mappingproject/" + mappingProjectId);
			return "error-msg";
		}
	}

	/**
	 * Displays an {@link AttributeMapping}
	 *
	 * @param mappingProjectId ID of the {@link MappingProject}
	 * @param target           name of the target entity
	 * @param source           name of the source entity
	 * @param targetAttribute  name of the target attribute
	 */
	@RequestMapping("/attributeMapping")
	public String viewAttributeMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute, Model model)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
		AttributeMapping attributeMapping = entityMapping.getAttributeMapping(targetAttribute);

		if (attributeMapping == null)
		{
			attributeMapping = entityMapping.addAttributeMapping(targetAttribute);
		}

		EntityMetaData refEntityMetaData = attributeMapping.getTargetAttributeMetaData().getRefEntity();
		if (refEntityMetaData != null)
		{
			Iterable<Entity> refEntities = new Iterable<Entity>()
			{
				@Override
				public Iterator<Entity> iterator()
				{
					return dataService.findAll(refEntityMetaData.getName()).iterator();
				}
			};
			model.addAttribute("categories", refEntities);
		}

		Multimap<Relation, OntologyTagObject> tagsForAttribute = ontologyTagService
				.getTagsForAttribute(entityMapping.getTargetEntityMetaData(),
						attributeMapping.getTargetAttributeMetaData());

		model.addAttribute("tags", tagsForAttribute.values());
		model.addAttribute("dataExplorerUri", menuReaderService.getMenu().findMenuItemPath(DataExplorerController.ID));
		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMapping", entityMapping);
		model.addAttribute("sourceAttributesSize",
				Iterables.size(entityMapping.getSourceEntityMetaData().getAtomicAttributes()));
		model.addAttribute("attributeMapping", attributeMapping);
		model.addAttribute("attributes",
				Lists.newArrayList(dataService.getEntityMetaData(source).getAtomicAttributes()));
		model.addAttribute("hasWritePermission", hasWritePermission(project, false));

		return VIEW_ATTRIBUTE_MAPPING;
	}

	@RequestMapping(value = "/attributemappingfeedback", method = RequestMethod.POST)
	public String attributeMappingFeedback(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute, @RequestParam(required = true) String algorithm,
			Model model)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);

		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);

		AttributeMapping algorithmTest;

		if (entityMapping.getAttributeMapping(targetAttribute) == null)
		{
			algorithmTest = entityMapping.addAttributeMapping(targetAttribute);
			algorithmTest.setAlgorithm(algorithm);
		}
		else
		{
			algorithmTest = entityMapping.getAttributeMapping(targetAttribute);
			algorithmTest.setAlgorithm(algorithm);
		}

		try
		{
			Collection<String> sourceAttributeNames = algorithmService.getSourceAttributeNames(algorithm);
			if (!sourceAttributeNames.isEmpty())
			{
				List<AttributeMetaData> sourceAttributes = sourceAttributeNames.stream()
						.map(attributeName -> entityMapping.getSourceEntityMetaData().getAttribute(attributeName))
						.collect(Collectors.toList());
				model.addAttribute("sourceAttributes", sourceAttributes);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		model.addAttribute("mappingProjectId", mappingProjectId);
		model.addAttribute("target", target);
		model.addAttribute("source", source);
		model.addAttribute("targetAttribute", dataService.getEntityMetaData(target).getAttribute(targetAttribute));

		FluentIterable<Entity> sourceEntities = FluentIterable.from(() -> dataService.findAll(source).iterator())
				.limit(10);

		ImmutableList<AlgorithmResult> algorithmResults = sourceEntities.transform(sourceEntity ->
		{
			try
			{
				return AlgorithmResult.createSuccess(
						algorithmService.apply(algorithmTest, sourceEntity, sourceEntity.getEntityMetaData()),
						sourceEntity);
			}
			catch (Exception e)
			{
				return AlgorithmResult.createFailure(e, sourceEntity);
			}
		}).toList();
		model.addAttribute("feedbackRows", algorithmResults);

		long missing = algorithmResults.stream().filter(r -> r.isSuccess() && r.getValue() == null).count();
		long success = algorithmResults.stream().filter(AlgorithmResult::isSuccess).count() - missing;
		long error = algorithmResults.size() - success - missing;

		model.addAttribute("success", success);
		model.addAttribute("missing", missing);
		model.addAttribute("error", error);
		model.addAttribute("dataexplorerUri", menuReaderService.getMenu().findMenuItemPath(DataExplorerController.ID));
		return VIEW_ATTRIBUTE_MAPPING_FEEDBACK;
	}

	/**
	 * Returns a view that allows the user to edit mappings involving xrefs / categoricals / strings
	 *
	 * @param mappingProjectId
	 * @param target
	 * @param source
	 * @param targetAttribute
	 * @param sourceAttribute
	 * @param model
	 */
	@RequestMapping(value = "/advancedmappingeditor", method = RequestMethod.POST)
	public String advancedMappingEditor(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute,
			@RequestParam(required = true) String sourceAttribute, @RequestParam String algorithm, Model model)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
		AttributeMapping attributeMapping = entityMapping.getAttributeMapping(targetAttribute);

		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMapping", entityMapping);
		model.addAttribute("attributeMapping", attributeMapping);

		// set variables for the target column in the mapping editor
		AttributeMetaData targetAttr = dataService.getEntityMetaData(target).getAttribute(targetAttribute);

		Stream<Entity> targetAttributeEntities;
		String targetAttributeIdAttribute = null;
		String targetAttributeLabelAttribute = null;

		if (EntityMetaDataUtils.isMultipleReferenceType(targetAttr))
		{
			targetAttributeEntities = dataService.findAll(
					dataService.getEntityMetaData(target).getAttribute(targetAttribute).getRefEntity().getName());

			targetAttributeIdAttribute = dataService.getEntityMetaData(target).getAttribute(targetAttribute)
					.getRefEntity().getIdAttribute().getName();

			targetAttributeLabelAttribute = dataService.getEntityMetaData(target).getAttribute(targetAttribute)
					.getRefEntity().getLabelAttribute().getName();
		}
		else
		{
			targetAttributeEntities = dataService.findAll(dataService.getEntityMetaData(target).getName());
			targetAttributeIdAttribute = dataService.getEntityMetaData(target).getIdAttribute().getName();
			targetAttributeLabelAttribute = dataService.getEntityMetaData(target).getLabelAttribute().getName();
		}

		model.addAttribute("targetAttributeEntities", new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return targetAttributeEntities.iterator();
			}
		});
		model.addAttribute("targetAttributeIdAttribute", targetAttributeIdAttribute);
		model.addAttribute("targetAttributeLabelAttribute", targetAttributeLabelAttribute);

		// set variables for the source column in the mapping editor
		AttributeMetaData sourceAttr = dataService.getEntityMetaData(source).getAttribute(sourceAttribute);

		Stream<Entity> sourceAttributeEntities;
		String sourceAttributeIdAttribute = null;
		String sourceAttributeLabelAttribute = null;

		if (EntityMetaDataUtils.isMultipleReferenceType(sourceAttr))
		{
			sourceAttributeEntities = dataService.findAll(
					dataService.getEntityMetaData(source).getAttribute(sourceAttribute).getRefEntity().getName());

			sourceAttributeIdAttribute = dataService.getEntityMetaData(source).getAttribute(sourceAttribute)
					.getRefEntity().getIdAttribute().getName();

			sourceAttributeLabelAttribute = dataService.getEntityMetaData(source).getAttribute(sourceAttribute)
					.getRefEntity().getLabelAttribute().getName();
		}
		else
		{
			sourceAttributeEntities = dataService.findAll(dataService.getEntityMetaData(source).getName());
			sourceAttributeIdAttribute = dataService.getEntityMetaData(source).getIdAttribute().getName();
			sourceAttributeLabelAttribute = dataService.getEntityMetaData(source).getLabelAttribute().getName();
		}

		List<Entity> sourceAttributeEntityList = sourceAttributeEntities.collect(toList());

		model.addAttribute("sourceAttributeEntities", sourceAttributeEntityList);
		model.addAttribute("numberOfSourceAttributes", sourceAttributeEntityList.size());
		model.addAttribute("sourceAttributeIdAttribute", sourceAttributeIdAttribute);
		model.addAttribute("sourceAttributeLabelAttribute", sourceAttributeLabelAttribute);

		// Check if the selected source attribute is aggregateable
		AttributeMetaData sourceAttributeAttributeMetaData = dataService.getEntityMetaData(source)
				.getAttribute(sourceAttribute);
		if (sourceAttributeAttributeMetaData.isAggregatable())
		{
			AggregateResult aggregate = dataService.aggregate(source,
					new AggregateQueryImpl().attrX(sourceAttributeAttributeMetaData).query(new QueryImpl<>()));
			List<Long> aggregateCounts = new ArrayList<>();
			for (List<Long> count : aggregate.getMatrix())
			{
				aggregateCounts.add(count.get(0));
			}
			model.addAttribute("aggregates", aggregateCounts);
		}

		model.addAttribute("target", target);
		model.addAttribute("source", source);
		model.addAttribute("targetAttribute", dataService.getEntityMetaData(target).getAttribute(targetAttribute));
		model.addAttribute("sourceAttribute", dataService.getEntityMetaData(source).getAttribute(sourceAttribute));
		model.addAttribute("hasWritePermission", hasWritePermission(project, false));

		CategoryMapping<String, String> categoryMapping = null;
		if (algorithm == null)
		{
			algorithm = attributeMapping.getAlgorithm();
		}
		try
		{
			categoryMapping = create(algorithm);
		}
		catch (Exception ignore)
		{
		}

		if (categoryMapping == null)
		{
			categoryMapping = createEmpty(sourceAttribute);
		}
		model.addAttribute("categoryMapping", categoryMapping);

		return VIEW_CATEGORY_MAPPING_EDITOR;
	}

	@RequestMapping(value = "/savecategorymapping", method = RequestMethod.POST)
	public
	@ResponseBody
	void saveCategoryMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute, @RequestParam(required = true) String algorithm)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(mappingProject))
		{
			MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
			EntityMapping mappingForSource = mappingTarget.getMappingForSource(source);
			AttributeMapping attributeMapping = mappingForSource.getAttributeMapping(targetAttribute);
			if (attributeMapping == null)
			{
				attributeMapping = mappingForSource.addAttributeMapping(targetAttribute);
			}
			attributeMapping.setAlgorithm(algorithm);
			attributeMapping.setAlgorithmState(AlgorithmState.CURATED);
			mappingService.updateMappingProject(mappingProject);
		}
	}

	/**
	 * Tests an algorithm by computing it for all entities in the source repository.
	 *
	 * @param mappingServiceRequest the {@link MappingServiceRequest} sent by the client
	 * @return Map with the results and size of the source
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/mappingattribute/testscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public
	@ResponseBody
	Map<String, Object> testScript(@RequestBody MappingServiceRequest mappingServiceRequest)
	{
		EntityMetaData targetEntityMetaData = dataService
				.getEntityMetaData(mappingServiceRequest.getTargetEntityName());
		AttributeMetaData targetAttribute = targetEntityMetaData != null ? targetEntityMetaData
				.getAttribute(mappingServiceRequest.getTargetAttributeName()) : null;
		Repository<Entity> sourceRepo = dataService.getRepository(mappingServiceRequest.getSourceEntityName());

		Iterable<AlgorithmEvaluation> algorithmEvaluations = algorithmService
				.applyAlgorithm(targetAttribute, mappingServiceRequest.getAlgorithm(), sourceRepo);

		List<Object> calculatedValues = newArrayList(
				Iterables.transform(algorithmEvaluations, AlgorithmEvaluation::getValue));

		return ImmutableMap.of("results", calculatedValues, "totalCount", Iterables.size(sourceRepo));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}

	/**
	 * <<<<<<< HEAD
	 * =======
	 * Generate algorithms based on semantic matches between attribute tags and descriptions
	 *
	 * @param mapping
	 * @param sourceEntityMetaData
	 * @param targetEntityMetaData
	 * @param attributes
	 * @param project
	 */
	private void autoGenerateAlgorithms(EntityMapping mapping, EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData, Iterable<AttributeMetaData> attributes, MappingProject project)
	{
		attributes.forEach(attribute -> algorithmService
				.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, attribute));
		mappingService.updateMappingProject(project);
	}

	/**
	 * >>>>>>> c73afd978c7f38e5829ca662b590e156bc19aa7b
	 * Lists the entities that may be added as new sources to this mapping project's selected target
	 *
	 * @param target the selected target
	 * @return
	 */
	private List<EntityMetaData> getNewSources(MappingTarget target)
	{
		return dataService.getMeta().getRepositories().map(Repository::getEntityMetaData)
				.filter(entityMetaData -> isValidSource(target, entityMetaData.getName())).collect(Collectors.toList());
	}

	private static boolean isValidSource(MappingTarget target, String name)
	{
		return !target.hasMappingFor(name);
	}

	private List<EntityMetaData> getEntityMetaDatas()
	{
		return dataService.getMeta().getRepositories().map(Repository::getEntityMetaData).collect(toList());
	}

	private List<EntityMetaData> getWritableEntityMetaDatas()
	{
		return getEntityMetaDatas().stream()
				.filter(emd -> dataService.getCapabilities(emd.getName()).contains(RepositoryCapability.WRITABLE))
				.collect(Collectors.toList());
	}

	private boolean hasWritePermission(MappingProject project)
	{
		return hasWritePermission(project, true);
	}

	private boolean hasWritePermission(MappingProject project, boolean logInfractions)
	{
		boolean result = currentUserIsSu() || project.getOwner().getUsername().equals(getCurrentUsername());
		if (logInfractions && !result)
		{
			LOG.warn("User " + getCurrentUsername() + " illegally tried to modify mapping project with id " + project
					.getIdentifier() + " owned by " + project.getOwner().getUsername());
		}
		return result;
	}

	private MolgenisUser getCurrentUser()
	{
		return molgenisUserService.getUser(getCurrentUsername());
	}

	private Map<String, List<OntologyTagObject>> getTagsForAttribute(String target, MappingProject project)
	{
		Map<String, List<OntologyTagObject>> attributeTagMap = new HashMap<>();
		for (AttributeMetaData amd : project.getMappingTarget(target).getTarget().getAtomicAttributes())
		{
			EntityMetaData targetMetaData = RunAsSystemProxy.runAsSystem(() -> dataService.getEntityMetaData(target));
			attributeTagMap.put(amd.getName(),
					newArrayList(ontologyTagService.getTagsForAttribute(targetMetaData, amd).values()));
		}

		return attributeTagMap;
	}

	private String getMappingServiceMenuUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(ID);
	}
}
