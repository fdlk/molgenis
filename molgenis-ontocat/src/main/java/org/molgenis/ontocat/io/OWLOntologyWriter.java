package org.molgenis.ontocat.io;

import java.io.File;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;

public interface OWLOntologyWriter
{
	public OWLClass createOWLClass(String iri, String label, List<String> synonyms, String description,
			OWLClass parentClass);

	public void saveOWLOntology(File file);
}
