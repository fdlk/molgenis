package org.molgenis.ui.jobs;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_HelloMessage.class)
public abstract class HelloMessage
{
	public abstract String getName();

	public static HelloMessage create(String name)
	{
		return new AutoValue_HelloMessage(name);
	}
}
