package org.molgenis.ontocat.xstream.ontologyterm;

public class XStreamLinks
{
	public static enum LinkType
	{
		SELF, ONTOLOGY, CHILDREN, PARENTS, DESCENDANTS, ANCESTORS, TREE, NOTES, MAPPINGS, UI
	}

	private final XStreamLinkContent self;
	private final XStreamLinkContent ontology;
	private final XStreamLinkContent children;
	private final XStreamLinkContent parents;
	private final XStreamLinkContent descendants;
	private final XStreamLinkContent ancestors;
	private final XStreamLinkContent tree;
	private final XStreamLinkContent notes;
	private final XStreamLinkContent mappings;
	private final XStreamLinkContent ui;

	public XStreamLinks(XStreamLinkContent self, XStreamLinkContent ontology, XStreamLinkContent children,
			XStreamLinkContent parents, XStreamLinkContent descendants, XStreamLinkContent ancestors,
			XStreamLinkContent tree, XStreamLinkContent notes, XStreamLinkContent mappings, XStreamLinkContent ui)
	{
		this.self = self;
		this.ontology = ontology;
		this.children = children;
		this.parents = parents;
		this.descendants = descendants;
		this.ancestors = ancestors;
		this.tree = tree;
		this.notes = notes;
		this.mappings = mappings;
		this.ui = ui;
	}

	public XStreamLinkContent getLink()
	{
		if (self != null) return self;
		if (ontology != null) return ontology;
		if (children != null) return children;
		if (parents != null) return parents;
		if (descendants != null) return descendants;
		if (ancestors != null) return ancestors;
		if (tree != null) return tree;
		if (notes != null) return notes;
		if (mappings != null) return mappings;
		if (ui != null) return ui;

		return null;
	}

	public LinkType getType()
	{
		if (self != null) return LinkType.SELF;
		if (ontology != null) return LinkType.ONTOLOGY;
		if (children != null) return LinkType.CHILDREN;
		if (parents != null) return LinkType.PARENTS;
		if (descendants != null) return LinkType.DESCENDANTS;
		if (ancestors != null) return LinkType.ANCESTORS;
		if (tree != null) return LinkType.TREE;
		if (notes != null) return LinkType.NOTES;
		if (mappings != null) return LinkType.MAPPINGS;
		if (ui != null) return LinkType.UI;

		throw new IllegalArgumentException("No LinkType is matched!");
	}
}
