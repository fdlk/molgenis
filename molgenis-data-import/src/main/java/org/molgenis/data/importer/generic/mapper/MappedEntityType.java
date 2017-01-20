package org.molgenis.data.importer.generic.mapper;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.data.meta.model.EntityType;

@AutoValue
public abstract class MappedEntityType
{
	public abstract EntityType getEntityType();

	public abstract ImmutableList<MappedAttribute> getMappedAttributes();

	public static MappedEntityType create(EntityType entityType, ImmutableList<MappedAttribute> mappedAttributes)
	{
		return new AutoValue_MappedEntityType(entityType, mappedAttributes);
	}
}
