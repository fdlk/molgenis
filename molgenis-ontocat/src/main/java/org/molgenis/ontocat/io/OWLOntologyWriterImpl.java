package org.molgenis.ontocat.io;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OWLOntologyWriterImpl implements OWLOntologyWriter
{
	private final static Logger LOG = Logger.getLogger(OWLOntologyWriterImpl.class);
	private final static String SYNONYM_ANNOTATION_PROPERTY = "http://www.geneontology.org/formats/oboInOwl#hasExactSynonym";
	private final OWLDataFactory owlDataFactory;
	private final OWLOntologyManager owlOntologyManager;
	private final OWLOntology ontology;

	public OWLOntologyWriterImpl(String ontologyIri) throws OWLOntologyCreationException
	{
		owlOntologyManager = OWLManager.createOWLOntologyManager();
		owlDataFactory = owlOntologyManager.getOWLDataFactory();
		ontology = owlOntologyManager.createOntology(IRI.create(ontologyIri));
	}

	public OWLClass createOWLClass(String iri, String label, List<String> synonyms, String description,
			OWLClass parentClass)
	{
		OWLClass cls = owlDataFactory.getOWLClass(IRI.create(iri));

		// Add label annotation
		if (StringUtils.isNotEmpty(label))
		{
			addAnnotation(cls, label, owlDataFactory.getRDFSLabel());
		}

		// Add synonym annotation
		if (synonyms.size() > 0)
		{
			OWLAnnotationProperty synonymProperty = owlDataFactory.getOWLAnnotationProperty(IRI
					.create(SYNONYM_ANNOTATION_PROPERTY));
			synonyms.forEach(synonym -> addAnnotation(cls, synonym, synonymProperty));
		}

		// add other annotations
		if (StringUtils.isNotEmpty(description))
		{
			addAnnotation(cls, description,
					owlDataFactory.getOWLAnnotationProperty(IRI.create(OWLRDFVocabulary.RDF_DESCRIPTION.toString())));
		}

		// add subclass relation
		if (parentClass == null)
		{
			parentClass = owlDataFactory.getOWLThing();
		}
		owlOntologyManager.applyChange(new AddAxiom(ontology, owlDataFactory.getOWLSubClassOfAxiom(cls, parentClass)));

		return cls;
	}

	public void saveOWLOntology(File file)
	{
		try
		{
			owlOntologyManager.saveOntology(ontology, IRI.create(file.toURI()));
		}
		catch (OWLOntologyStorageException e)
		{
			LOG.error(e.getMessage());
		}
	}

	private void addAnnotation(OWLClass owlClass, String annotation, OWLAnnotationProperty property)
	{
		OWLAnnotation commentAnno = owlDataFactory.getOWLAnnotation(property,
				owlDataFactory.getOWLLiteral(annotation, "en"));
		OWLAxiom ax = owlDataFactory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), commentAnno);
		owlOntologyManager.applyChange(new AddAxiom(ontology, ax));
	}
}
