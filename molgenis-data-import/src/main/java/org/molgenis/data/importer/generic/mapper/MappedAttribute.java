package org.molgenis.data.importer.generic.mapper;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;

@AutoValue
public abstract class MappedAttribute
{
	public abstract int getIndex();

	public abstract String getHeader();

	public abstract Attribute getAttribute();

	public static MappedAttribute create(int index, String header, Attribute attr)
	{
		return new AutoValue_MappedAttribute(index, header, attr);
	}
}
