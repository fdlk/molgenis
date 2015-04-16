package org.molgenis.ontocat.xstream.ontology;

import java.util.List;

public class XStreamOntologyCollection
{
	private final List<XStreamOntology> ontologyCollection;

	public XStreamOntologyCollection(List<XStreamOntology> ontologyCollection)
	{
		this.ontologyCollection = ontologyCollection;
	}

	public List<XStreamOntology> getOntologyCollection()
	{
		return ontologyCollection;
	}
}
