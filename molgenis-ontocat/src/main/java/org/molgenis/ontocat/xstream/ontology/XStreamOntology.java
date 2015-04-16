package org.molgenis.ontocat.xstream.ontology;


public class XStreamOntology
{
	private final String id;
	private final String name;
	private final String acronym;
	private final String type;
	private final Boolean summaryOnly;

	public XStreamOntology(String acronym, String name, String id, String type, Boolean summaryOnly)
	{
		this.id = id;
		this.name = name;
		this.acronym = acronym;
		this.type = type;
		this.summaryOnly = summaryOnly;
	}

	public Boolean getSummaryOnly()
	{
		return summaryOnly;
	}

	public String getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getAcronym()
	{
		return acronym;
	}
}
