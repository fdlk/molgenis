package org.molgenis.ontocat.bioportal;

import java.util.List;

import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;

public class BioportalOntologyTerm extends OntologyTerm
{
	private final String id;
	private final String uri;
	private final String label;
	private final String description;
	private final List<String> synonyms;
	private final Ontology ontology;

	public BioportalOntologyTerm(String id, String uri, String label, String description, List<String> synonyms,
			Ontology ontology)
	{
		this.id = id;
		this.uri = uri;
		this.label = label;
		this.description = description;
		this.synonyms = synonyms;
		this.ontology = ontology;
	}

	public String getId()
	{
		return id;
	}

	public String getIRI()
	{
		return uri;
	}

	public String getLabel()
	{
		return label;
	}

	public String getDescription()
	{
		return description;
	}

	public List<String> getSynonyms()
	{
		return synonyms;
	}

	public Ontology getOntology()
	{
		return ontology;
	}
}
