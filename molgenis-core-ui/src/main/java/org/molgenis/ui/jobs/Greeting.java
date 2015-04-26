package org.molgenis.ui.jobs;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Greeting.class)
public abstract class Greeting
{
	public abstract String getContent();

	public static Greeting create(String content)
	{
		return new AutoValue_Greeting(content);
	}
}
