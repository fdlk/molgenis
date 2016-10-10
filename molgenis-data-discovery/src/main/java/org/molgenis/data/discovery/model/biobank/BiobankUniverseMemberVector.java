package org.molgenis.data.discovery.model.biobank;

import com.google.auto.value.AutoValue;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_BiobankUniverseMemberVector.class)
public abstract class BiobankUniverseMemberVector implements Clusterable
{
	public abstract String getIdentifier();

	public abstract BiobankSampleCollection getBiobankSampleCollection();

	public abstract double[] getPoint();

	public static BiobankUniverseMemberVector create(String identifier, BiobankSampleCollection biobankSampleCollection,
			double[] point)
	{
		return new AutoValue_BiobankUniverseMemberVector(identifier, biobankSampleCollection, point);
	}
}
