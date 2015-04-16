package org.molgenis.ontocat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.ontocat.bioportal.BioPortalOntologyService;
import org.molgenis.ontocat.io.OWLOntologyWriter;
import org.molgenis.ontocat.io.OWLOntologyWriterImpl;
import org.molgenis.ontocat.ontologyservice.OntologyService;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class App
{
	public static void main(String[] args) throws FileNotFoundException, OWLOntologyCreationException
	{
		if (args.length == 2)
		{
			String ontologyAcronym = args[0];
			File output = new File(args[1]);
			if (StringUtils.isNotEmpty(ontologyAcronym) && output.getParentFile().exists())
			{
				OntologyService os = new BioPortalOntologyService();
				Ontology ontology = os.getOntology(ontologyAcronym);
				OWLOntologyWriter writer = new OWLOntologyWriterImpl(ontology.getIRI());
				List<OntologyTerm> rootTerms = os.getRootTerms(ontologyAcronym);
				rootTerms.forEach(ot -> recursive(ot, os, writer, null));

				writer.saveOWLOntology(output);
			}
		}
	}

	private static void recursive(OntologyTerm ontologyTerm, OntologyService os, OWLOntologyWriter writer,
			OWLClass parentClass)
	{
		OWLClass cls = writer.createOWLClass(ontologyTerm.getIRI(), ontologyTerm.getLabel(),
				ontologyTerm.getSynonyms(), ontologyTerm.getDescription(), parentClass);

		for (OntologyTerm ot : os.getChildren(ontologyTerm))
		{
			recursive(ot, os, writer, cls);
		}
	}
}
