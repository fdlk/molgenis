package org.molgenis.data.importer.generic.mapper;

import org.molgenis.data.Entity;
import org.molgenis.data.importer.table.Row;

public interface RowToEntityMapper
{
	Entity create(Row row, MappedEntityType mappedEntityType);
}
