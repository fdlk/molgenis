package org.molgenis.ontocat.bioportal;

import org.molgenis.ontology.core.model.Ontology;

public class BioportalOntology extends Ontology
{
	private final String id;
	private final String iri;
	private final String label;

	public BioportalOntology(String id, String iri, String label)
	{
		this.id = id;
		this.iri = iri;
		this.label = label;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getIRI()
	{
		return iri;
	}

	@Override
	public String getName()
	{
		return label;
	}
}
