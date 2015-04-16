package org.molgenis.ontocat.io;

import java.io.File;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.Lists;

public class TestOWLWriter
{
	public static void main(String[] args) throws OWLOntologyCreationException
	{
		OWLOntologyWriter writer = new OWLOntologyWriterImpl("http://www.molgenis.org/");

		OWLClass createOWLClass = writer.createOWLClass("http://www.molgenis.org/class/1", "class 1",
				Lists.newArrayList("class one"), null, null);

		writer.createOWLClass("http://www.molgenis.org/class/2", "class 2", Lists.newArrayList("class two"), null,
				createOWLClass);

		writer.saveOWLOntology(new File("/Users/chaopang/Desktop/test_writer.owl"));
	}
}
