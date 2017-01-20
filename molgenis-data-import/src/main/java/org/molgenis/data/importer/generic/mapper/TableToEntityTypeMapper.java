package org.molgenis.data.importer.generic.mapper;

import org.molgenis.data.importer.table.Table;

public interface TableToEntityTypeMapper
{
	MappedEntityType create(Table table);
}
