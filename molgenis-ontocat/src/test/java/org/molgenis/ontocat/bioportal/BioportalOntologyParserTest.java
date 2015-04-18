package org.molgenis.ontocat.bioportal;

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

public class BioportalOntologyParserTest
{
	private String json_5_ontologies;
	private String json_1_ontology;
	private String json_1_ontologyTerm;
	private String json_3_ontologyTerms;

	@BeforeClass
	public void setup() throws JsonIOException, JsonSyntaxException, FileNotFoundException
	{
		JsonParser parser = new JsonParser();
		ClassLoader classLoader = getClass().getClassLoader();
		json_5_ontologies = parser.parse(
				new FileReader(new File(classLoader.getResource("3_ontologies.json").getFile()))).toString();
		json_1_ontology = parser.parse(new FileReader(new File(classLoader.getResource("ontology.json").getFile())))
				.toString();
		json_1_ontologyTerm = parser.parse(
				new FileReader(new File(classLoader.getResource("ontologyTerm.json").getFile()))).toString();
		json_3_ontologyTerms = parser.parse(
				new FileReader(new File(classLoader.getResource("3_ontologyTerms.json").getFile()))).toString();
	}

	@Test
	public void conceptIriToId()
	{
		assertEquals(BioportalOntologyParser.conceptIriToId("http://www.molgenis.org/123"), "123");
		assertEquals(BioportalOntologyParser.conceptIriToId("http://www.molgenis.org#456"), "456");
	}

	@Test
	public void convertJsonStringToOntologies()
	{
		List<Ontology> ontologies = BioportalOntologyParser.convertJsonStringToOntologies(json_5_ontologies);
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
	public void convertJsonStringToOntology()
	{
		Ontology ontology = BioportalOntologyParser.convertJsonStringToOntology(json_1_ontology);
		assertEquals(ontology.getId(), "TMO");
		assertEquals(ontology.getName(), "Translational Medicine Ontology");
		assertEquals(ontology.getIRI(), "http://data.bioontology.org/ontologies/TMO");
	}

	@Test
	public void convertJsonStringToOntologyTerm()
	{
		OntologyTerm ontologyTerm = BioportalOntologyParser.convertJsonStringToOntologyTerm(json_1_ontologyTerm);
		assertEquals(ontologyTerm.getLabel(), "Social context");
		assertEquals(ontologyTerm.getIRI(), "http://purl.bioontology.org/ontology/SNOMEDCT/48176007");
		assertEquals(ontologyTerm.getDescription(), StringUtils.EMPTY);
		assertEquals(ontologyTerm.getSynonyms().size(), 1);
		assertEquals(ontologyTerm.getSynonyms().get(0), "Social context (social concept)");
	}

	@Test
	public void convertJsonStringToOntologyTerms()
	{
		List<OntologyTerm> ontologyTerms = BioportalOntologyParser
				.convertJsonStringToOntologyTerms(json_3_ontologyTerms);
		assertEquals(ontologyTerms.size(), 3);

		OntologyTerm ontologyTerm_1 = ontologyTerms.get(0);
		assertEquals(ontologyTerm_1.getLabel(), "Crushing injury of other parts of neck");
		assertEquals(ontologyTerm_1.getIRI(), "http://purl.bioontology.org/ontology/ICD10/S17.8");
		assertEquals(ontologyTerm_1.getDescription(), StringUtils.EMPTY);
		assertEquals(ontologyTerm_1.getSynonyms().size(), 0);

		OntologyTerm ontologyTerm_2 = ontologyTerms.get(1);
		assertEquals(ontologyTerm_2.getLabel(), "Gangrene and necrosis of lung");
		assertEquals(ontologyTerm_2.getIRI(), "http://purl.bioontology.org/ontology/ICD10/J85.0");
		assertEquals(ontologyTerm_2.getDescription(), StringUtils.EMPTY);
		assertEquals(ontologyTerm_2.getSynonyms().size(), 0);

		OntologyTerm ontologyTerm_3 = ontologyTerms.get(2);
		assertEquals(ontologyTerm_3.getLabel(), "Physical object");
		assertEquals(ontologyTerm_3.getIRI(), "http://purl.bioontology.org/ontology/SNOMEDCT/260787004");
		assertEquals(ontologyTerm_3.getDescription(), StringUtils.EMPTY);
		assertEquals(ontologyTerm_3.getSynonyms().size(), 5);
		assertEquals(
				ontologyTerm_3.getSynonyms().containsAll(
						Arrays.asList("Artefacts", "Artifacts", "Physical object (physical object)",
								"Physical objects", "Objects")), true);
	}
}
