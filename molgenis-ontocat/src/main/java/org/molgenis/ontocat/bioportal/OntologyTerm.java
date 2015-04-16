package org.molgenis.ontocat.bioportal;

import java.util.List;

public interface OntologyTerm
{
	public abstract String getIRI();

	public abstract String getLabel();

	public abstract String getDescription();

	public abstract List<String> getSynonyms();
}
