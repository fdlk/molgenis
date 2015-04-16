package org.molgenis.ontocat.ontologyservice;

import java.util.List;

import org.molgenis.ontocat.bioportal.Ontology;
import org.molgenis.ontocat.bioportal.OntologyTerm;

public interface OntologyService
{
	public List<Ontology> getOntologies();

	public Ontology getOntology(String identifier);

	public List<OntologyTerm> getRootTerms(String ontologyAccession);

	public List<OntologyTerm> getChildren(OntologyTerm ontologyTerm);

	public int getProxyCountForOntology(String ontologyAcronym);
}
