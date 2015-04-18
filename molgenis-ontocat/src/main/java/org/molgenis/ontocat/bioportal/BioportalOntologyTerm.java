package org.molgenis.ontocat.bioportal;

import java.util.List;

public class BioportalOntologyTerm implements OntologyTerm
{
	private final String id;
	private final String uri;
	private final String label;
	private final String description;
	private final List<String> synonyms;
	private final String ontologyAcronym;

	public BioportalOntologyTerm(String id, String uri, String label, String description, List<String> synonyms,
			String ontologyAcronym)
	{
		this.id = id;
		this.uri = uri;
		this.label = label;
		this.description = description;
		this.synonyms = synonyms;
		this.ontologyAcronym = ontologyAcronym;
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

	public String getOntologyAcronymy()
	{
		return ontologyAcronym;
	}
}
