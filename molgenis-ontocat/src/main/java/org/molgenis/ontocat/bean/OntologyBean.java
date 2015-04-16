package org.molgenis.ontocat.bean;

public class OntologyBean
{
	private final String id;
	private final String name;
	private final String acronym;
	private final String type;
	private final Boolean summaryOnly;

	public OntologyBean(String acronym, String name, String id, String type, Boolean summaryOnly)
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
