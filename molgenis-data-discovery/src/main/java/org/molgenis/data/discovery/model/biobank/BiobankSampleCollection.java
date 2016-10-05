package org.molgenis.data.discovery.model.biobank;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_BiobankSampleCollection.class)
public abstract class BiobankSampleCollection
{
	public abstract String getName();

	public static BiobankSampleCollection create(String name)
	{
		return new AutoValue_BiobankSampleCollection(name);
	}
}
