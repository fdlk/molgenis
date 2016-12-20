package org.molgenis.data.importer.generic;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.EntityType;

import java.util.Collection;

@AutoValue
public abstract class ImportResult
{
	public abstract Collection<EntityType> getEntityTypes();

	public static ImportResult create(Collection<EntityType> entityTypes)
	{
		return new AutoValue_ImportResult(entityTypes);
	}
}
