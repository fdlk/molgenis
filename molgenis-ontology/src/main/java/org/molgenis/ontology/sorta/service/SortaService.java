package org.molgenis.ontology.sorta.service;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.sorta.bean.SortaHit;

public interface SortaService
{
	/**
	 * Find a list of relevant {@link OntologyTermImpl}s using lexical matching (elasticsearch + ngram) based on given
	 * ontologyIri and a set of query inputs (name, synonym, ontology database id, e.g. hpo, omim).
	 * 
	 * @param ontologyIri
	 * @param inputEntity
	 * @return a list of {@link SortaHit}s
	 */
	List<SortaHit> findOntologyTermEntities(String ontologyIri, Entity inputEntity);

}