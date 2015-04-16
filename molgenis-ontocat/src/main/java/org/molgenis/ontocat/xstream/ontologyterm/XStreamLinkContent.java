package org.molgenis.ontocat.xstream.ontologyterm;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class XStreamLinkContent
{
	@XStreamAsAttribute
	private final String href;
	@XStreamAsAttribute
	private final String ref;

	public XStreamLinkContent(String href, String ref)
	{
		this.href = href;
		this.ref = ref;
	}

	public String getHref()
	{
		return href;
	}

	public String getRef()
	{
		return ref;
	}
}
