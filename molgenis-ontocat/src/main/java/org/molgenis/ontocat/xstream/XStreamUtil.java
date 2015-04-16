package org.molgenis.ontocat.xstream;

import java.util.ArrayList;

import org.molgenis.ontocat.xstream.ontology.XStreamOntology;
import org.molgenis.ontocat.xstream.ontology.XStreamOntologyCollection;
import org.molgenis.ontocat.xstream.ontologyterm.XStreamLinkContent;
import org.molgenis.ontocat.xstream.ontologyterm.XStreamLinks;
import org.molgenis.ontocat.xstream.ontologyterm.XStreamLinks.LinkType;
import org.molgenis.ontocat.xstream.ontologyterm.XStreamOntologyTerm;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;

public class XStreamUtil
{
	private final XStream xStream;
	{
		xStream = new XStream();
		xStream.alias("ontologyCollection", XStreamOntologyCollection.class);
		xStream.alias("collection", XStreamOntologyCollection.class);
		xStream.alias("ontology", XStreamOntology.class);
		xStream.addImplicitCollection(XStreamOntologyCollection.class, "ontologyCollection");
		xStream.omitField(XStreamOntology.class, "linksCollection");
		xStream.omitField(XStreamOntology.class, "administeredByCollection");

		xStream.alias("class", XStreamOntologyTerm.class);
		xStream.alias("synonymCollection", ArrayList.class);
		xStream.alias("synonym", String.class);
		xStream.alias("linksCollection", ArrayList.class);
		xStream.alias("links", XStreamLinks.class);
		Lists.newArrayList(LinkType.values()).forEach(
				type -> xStream.alias(type.toString().toLowerCase(), XStreamLinkContent.class));
		xStream.aliasAttribute(XStreamLinkContent.class, "href", "href");
		xStream.aliasAttribute(XStreamLinkContent.class, "ref", "ref");
		xStream.addImplicitCollection(XStreamOntologyTerm.class, "linksCollection");

		xStream.omitField(XStreamOntologyTerm.class, "definitionCollection");
		xStream.omitField(XStreamOntologyTerm.class, "cuiCollection");
		xStream.omitField(XStreamOntologyTerm.class, "semanticTypeCollection");
		xStream.omitField(XStreamOntologyTerm.class, "obsolete");
		xStream.omitField(XStreamOntologyTerm.class, "type");
	}
}
