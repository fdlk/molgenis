package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MatchedAttributeTagGroup.class)
public abstract class MatchedAttributeTagGroup implements Comparable<MatchedAttributeTagGroup>
{
	public abstract TagGroup getTarget();

	public abstract TagGroup getSource();

	public abstract Double getSimilarity();

	public static MatchedAttributeTagGroup create(TagGroup target, TagGroup source, Double similarity)
	{
		return new AutoValue_MatchedAttributeTagGroup(target, source, similarity);
	}

	public int compareTo(MatchedAttributeTagGroup o)
	{
		return Double.compare(o.getSimilarity(), getSimilarity());
	}
}
