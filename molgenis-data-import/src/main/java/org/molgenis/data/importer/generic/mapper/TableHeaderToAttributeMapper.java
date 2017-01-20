package org.molgenis.data.importer.generic.mapper;

public interface TableHeaderToAttributeMapper
{
	MappedAttribute create(int index, String header);
}
