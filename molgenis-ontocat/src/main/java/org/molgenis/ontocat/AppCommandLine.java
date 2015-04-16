package org.molgenis.ontocat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
				final int totalNumberOfClasses = os.getProxyCountForOntology(ontologyAcronym);
				Ontology ontology = os.getOntology(ontologyAcronym);
				OWLOntologyWriter writer = new OWLOntologyWriterImpl(ontology.getIRI());
				List<OntologyTerm> rootTerms = os.getRootTerms(ontologyAcronym);
				rootTerms.forEach(ot -> recursive(ot, os, writer, null, new HashSet<String>(), totalNumberOfClasses));

				writer.saveOWLOntology(output);
			}
		}
		else
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter
					.printHelp(
							"java -jar ontologyDownloader.jar",
							"where options include:",
							options,
							"\nTo download big ontologies such as SNOMEDCT, it is suggested to increase the maximum amount of memory allocated to java e.g. -Xmx2G");
		}
	}

	private static void recursive(OntologyTerm ontologyTerm, OntologyService os, OWLOntologyWriter writer,
			OWLClass parentClass, Set<String> ontologyTermIris, final int totalNumberOfClasses)
	{
		OWLClass cls = writer.createOWLClass(ontologyTerm.getIRI(), ontologyTerm.getLabel(),
				ontologyTerm.getSynonyms(), ontologyTerm.getDescription(), parentClass);

		if (!ontologyTermIris.contains(ontologyTerm.getIRI()))
		{
			ontologyTermIris.add(ontologyTerm.getIRI());
		}

		if (ontologyTermIris.size() % 50 == 0)
		{
			System.out.println("INFO : " + ontologyTermIris.size() + " out of " + totalNumberOfClasses
					+ " classes have been downloaded!");
		}

		for (OntologyTerm ot : os.getChildren(ontologyTerm))
		{
			recursive(ot, os, writer, cls, ontologyTermIris, totalNumberOfClasses);
		}
	}
}
