package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_BiobankSampleCollectionSimilarity.class)
public abstract class BiobankSampleCollectionSimilarity implements Comparable<BiobankSampleCollectionSimilarity>
{
	public abstract BiobankSampleCollection getTarget();

	public abstract BiobankSampleCollection getSource();

	public abstract float getSimilarity();

	public abstract String getLabel();

	public static BiobankSampleCollectionSimilarity create(BiobankSampleCollection target,
			BiobankSampleCollection source, float similarity, String label)
	{
		return new AutoValue_BiobankSampleCollectionSimilarity(target, source, similarity, label);
	}

	public int compareTo(BiobankSampleCollectionSimilarity o)
	{
		int compareTo = getTarget().getName().compareTo(o.getTarget().getName());
		if (compareTo == 0)
		{
			compareTo = getSource().getName().compareTo(o.getSource().getName());
		}
		return compareTo;
	}
}
