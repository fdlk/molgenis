package org.molgenis.data.importer.generic.mapper;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.Entity;
import org.molgenis.data.importer.table.Row;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.stereotype.Component;

@Component
public class RowToEntityMapperImpl implements RowToEntityMapper
{
	@Override
	public Entity create(Row row, MappedEntityType mappedEntityType)
	{
		EntityType entityType = mappedEntityType.getEntityType();
		ImmutableList<MappedAttribute> mappedAttrs = mappedEntityType.getMappedAttributes();

		Entity entity = new DynamicEntity(entityType);
		mappedAttrs.forEach(mappedAttr ->
		{
			String attrName = mappedAttr.getAttribute().getName();
			String value = row.getValue(mappedAttr.getIndex());
			entity.set(attrName, value);
		});

		return entity;
	}
}
