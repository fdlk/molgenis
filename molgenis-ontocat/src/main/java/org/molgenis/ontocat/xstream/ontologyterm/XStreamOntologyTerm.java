package org.molgenis.ontocat.xstream.ontologyterm;

import java.util.List;

public class XStreamOntologyTerm
{
	private final String id;
	private final String prefLabel;
	private final List<String> synonymCollection;
	private final List<XStreamLinks> linksCollection;

	public XStreamOntologyTerm(String id, String prefLabel, List<String> synonymCollection,
			List<XStreamLinks> linksCollection)
	{
		this.id = id;
		this.prefLabel = prefLabel;
		this.synonymCollection = synonymCollection;
		this.linksCollection = linksCollection;
	}

	public String getId()
	{
		return id;
	}

	public String getPrefLabel()
	{
		return prefLabel;
	}

	public List<String> getSynonymCollection()
	{
		return synonymCollection;
	}

	public List<XStreamLinks> getLinksCollection()
	{
		return linksCollection;
	}
}
