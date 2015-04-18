package org.molgenis.ontocat.bioportal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontocat.bioportal.BioportalOntologyParser.convertJsonStringToOntologyTerms;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class BioPortalOntologyServiceTest
{
	BioPortalOntologyHttpClient httpClient;

	BioPortalOntologyService os;

	@BeforeClass
	public void setup() throws JsonIOException, JsonSyntaxException, FileNotFoundException
	{

		httpClient = mock(BioPortalOntologyHttpClient.class);

		os = new BioPortalOntologyService(httpClient);

		JsonParser parser = new JsonParser();
		ClassLoader classLoader = getClass().getClassLoader();

		String roots = parser.parse(
				new FileReader(new File(classLoader.getResource("ontologyTerm_roots.json").getFile()))).toString();

		String ontology = parser.parse(new FileReader(new File(classLoader.getResource("ontology.json").getFile())))
				.toString();

		String children = parser.parse(
				new FileReader(new File(classLoader.getResource("ontologyTerm_children.json").getFile()))).toString();

		String count = parser.parse(
				new FileReader(new File(classLoader.getResource("ontologyTerm_count.json").getFile()))).toString();

		String ontologies = parser.parse(
				new FileReader(new File(classLoader.getResource("3_ontologies.json").getFile()))).toString();
		when(httpClient.getHttpResponse("http://data.bioontology.org/ontologies/SNOMEDCT/classes/roots")).thenReturn(
				roots);
		when(httpClient.getHttpResponse("http://data.bioontology.org/ontologies")).thenReturn(ontologies);

		when(httpClient.getHttpResponse("http://data.bioontology.org/ontologies/TMO")).thenReturn(ontology);

		when(
				httpClient
						.recursivelyPageChildren("http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F260787004/children"))
				.thenReturn(convertJsonStringToOntologyTerms(children));

		when(httpClient.getHttpResponse("http://data.bioontology.org/ontologies/SNOMEDCT/classes")).thenReturn(count);
	}

	@Test
	public void getChildren()
	{
		List<OntologyTerm> rootTerms = os.getRootTerms("SNOMEDCT");
		OntologyTerm ontologyTerm = rootTerms.get(0);
		assertEquals(ontologyTerm.getIRI(), "http://purl.bioontology.org/ontology/SNOMEDCT/260787004");
		List<OntologyTerm> children = os.getChildren(ontologyTerm);
		assertEquals(children.size(), 8);

		OntologyTerm child_ot_1 = children.get(0);
		assertEquals(child_ot_1.getLabel(), "Device");
		assertEquals(child_ot_1.getIRI(), "http://purl.bioontology.org/ontology/SNOMEDCT/49062001");
		assertEquals(child_ot_1.getDescription(), StringUtils.EMPTY);
		assertEquals(child_ot_1.getSynonyms().size(), 1);
		assertEquals(child_ot_1.getSynonyms().get(0), "Device (physical object)");
	}

	@Test
	public void getOntologies()
	{
		List<Ontology> ontologies = os.getOntologies();
		assertEquals(ontologies.size(), 3);

		Ontology ontology_1 = ontologies.get(0);
		assertEquals(ontology_1.getId(), "ICO");
		assertEquals(ontology_1.getName(), "Informed Consent Ontology");
		assertEquals(ontology_1.getIRI(), "http://data.bioontology.org/ontologies/ICO");

		Ontology ontology_2 = ontologies.get(1);
		assertEquals(ontology_2.getId(), "GEOSPECIES");
		assertEquals(ontology_2.getName(), "GeoSpecies Ontology");
		assertEquals(ontology_2.getIRI(), "http://data.bioontology.org/ontologies/GEOSPECIES");

		Ontology ontology_3 = ontologies.get(2);
		assertEquals(ontology_3.getId(), "TEO");
		assertEquals(ontology_3.getName(), "Time Event Ontology");
		assertEquals(ontology_3.getIRI(), "http://data.bioontology.org/ontologies/TEO");
	}

	@Test
	public void getOntology()
	{
		Ontology ontology = os.getOntology("TMO");
		assertEquals(ontology.getId(), "TMO");
		assertEquals(ontology.getName(), "Translational Medicine Ontology");
		assertEquals(ontology.getIRI(), "http://data.bioontology.org/ontologies/TMO");
	}

	@Test
	public void getProxyCountForOntology()
	{
		assertEquals(os.getProxyCountForOntology("SNOMEDCT"), 303050);
	}

	@Test
	public void getRootTerms()
	{
		List<OntologyTerm> rootTerms = os.getRootTerms("SNOMEDCT");
		assertEquals(rootTerms.size(), 19);

		OntologyTerm ontologyTerm = rootTerms.get(0);
		assertEquals(ontologyTerm.getLabel(), "Physical object");
		assertEquals(ontologyTerm.getIRI(), "http://purl.bioontology.org/ontology/SNOMEDCT/260787004");
		assertEquals(ontologyTerm.getDescription(), StringUtils.EMPTY);
		assertEquals(ontologyTerm.getSynonyms().size(), 5);
		assertEquals(
				ontologyTerm.getSynonyms().containsAll(
						Arrays.asList("Artefacts", "Artifacts", "Physical object (physical object)",
								"Physical objects", "Objects")), true);
	}
}