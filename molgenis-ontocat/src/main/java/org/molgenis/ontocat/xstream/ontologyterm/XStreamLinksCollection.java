package org.molgenis.ontocat.xstream.ontologyterm;

import java.util.List;

public class XStreamLinksCollection
{
	private final List<XStreamLinks> links;

	public XStreamLinksCollection(List<XStreamLinks> links)
	{
		this.links = links;
	}

	public List<XStreamLinks> getLinks()
	{
		return links;
	}
}
