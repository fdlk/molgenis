package org.molgenis.ontocat.ontologyservice;

import java.util.List;

import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;

public interface OntologyService
{
	public List<Ontology> getOntologies();

	public Ontology getOntology(String identifier);

	public List<OntologyTerm> getRootTerms(String ontologyAccession);

	public List<OntologyTerm> getChildren(OntologyTerm ontologyTerm);
}
