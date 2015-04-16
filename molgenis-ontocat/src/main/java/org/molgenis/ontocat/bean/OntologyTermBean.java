package org.molgenis.ontocat.bean;

import java.util.List;
import java.util.Map;

public class OntologyTermBean
{
	public static enum LinkType
	{
		SELF, ONTOLOGY, CHILDREN, PARENTS, DESCENDANTS, ANCESTORS, TREE, NOTES, MAPPINGS, UI
	}

	private final String prefLabel;
	private final List<String> synonym;
	private final List<String> definition;
	private final Map<String, Object> links;

	public OntologyTermBean(String prefLabel, List<String> synonym, List<String> definition, Map<String, Object> links)
	{
		this.prefLabel = prefLabel;
		this.synonym = synonym;
		this.definition = definition;
		this.links = links;
	}

	public String getPrefLabel()
	{
		return prefLabel;
	}

	public List<String> getSynonym()
	{
		return synonym;
	}

	public List<String> getDefinition()
	{
		return definition;
	}

	public String getSelf()
	{
		return links.get(LinkType.SELF.toString().toLowerCase()).toString();
	}

	public String getOntology()
	{
		return links.get(LinkType.ONTOLOGY.toString().toLowerCase()).toString();
	}

	public String getChildren()
	{
		return links.get(LinkType.CHILDREN.toString().toLowerCase()).toString();
	}
}
