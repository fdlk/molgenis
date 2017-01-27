package org.molgenis.data.importer.generic.meta;

import org.molgenis.data.meta.model.EntityType;

import java.util.List;

public interface MetadataImprover
{
	List<EntityType> improveMetadata(List<EntityType> entityTypes);
}
