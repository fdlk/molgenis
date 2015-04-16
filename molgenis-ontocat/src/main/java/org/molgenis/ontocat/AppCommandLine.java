package org.molgenis.ontocat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.ontocat.bioportal.BioPortalOntologyService;
import org.molgenis.ontocat.bioportal.Ontology;
import org.molgenis.ontocat.bioportal.OntologyTerm;
import org.molgenis.ontocat.io.OWLOntologyWriter;
import org.molgenis.ontocat.io.OWLOntologyWriterImpl;
import org.molgenis.ontocat.ontologyservice.OntologyService;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class AppCommandLine
{
	public static void main(String[] args) throws FileNotFoundException, OWLOntologyCreationException, ParseException
	{
		OntologyService os = new BioPortalOntologyService();
		Options options = new Options();
		options.addOption(new Option("list", "List all the ontologies available at BioPortal"));
		options.addOption(new Option("acronym", true, "provide the acronym of ontology for which you want to download"));
		options.addOption(new Option("filePath", true, "provide the filePath for downloaded ontology"));
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("list"))
		{
			List<Ontology> ontologies = os.getOntologies();
			System.out.println("Acronym\t\tOntology Name\t\tOntology IRI");
			ontologies.forEach(ontology -> System.out.println(ontology.getId() + "\t\t" + ontology.getName() + "\t\t"
					+ ontology.getIRI()));
		}
		else if (cmd.hasOption("acronym") && cmd.hasOption("filePath"))
		{
			String ontologyAcronym = cmd.getOptionValue("acronym");
			File output = new File(cmd.getOptionValue("filePath"));
			if (StringUtils.isNotEmpty(ontologyAcronym) && output.getParentFile().exists())
			{
				Ontology ontology = os.getOntology(ontologyAcronym);
				OWLOntologyWriter writer = new OWLOntologyWriterImpl(ontology.getIRI());
				List<OntologyTerm> rootTerms = os.getRootTerms(ontologyAcronym);
				rootTerms.forEach(ot -> recursive(ot, os, writer, null, new AtomicInteger(0)));

				writer.saveOWLOntology(output);
			}
		}
		else
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar ontologyDownloader.jar", options);
		}
	}

	private static void recursive(OntologyTerm ontologyTerm, OntologyService os, OWLOntologyWriter writer,
			OWLClass parentClass, AtomicInteger atomicInteger)
	{
		OWLClass cls = writer.createOWLClass(ontologyTerm.getIRI(), ontologyTerm.getLabel(),
				ontologyTerm.getSynonyms(), ontologyTerm.getDescription(), parentClass);

		if (atomicInteger.incrementAndGet() % 500 == 0)
		{
			System.out.println("INFO : " + atomicInteger.get() + " of classes have been downloaded!");
		}

		for (OntologyTerm ot : os.getChildren(ontologyTerm))
		{
			recursive(ot, os, writer, cls, atomicInteger);
		}
	}
}
