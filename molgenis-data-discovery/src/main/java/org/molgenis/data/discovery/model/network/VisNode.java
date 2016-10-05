package org.molgenis.data.discovery.model.network;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_VisNode.class)
public abstract class VisNode
{
	public abstract String getId();

	public abstract String getLabel();

	public abstract int getValue();

	public abstract String getShape();

	public static VisNode create(String id, String label, int value, String shape)
	{
		return new AutoValue_VisNode(id, label, value, shape);
	}
}
