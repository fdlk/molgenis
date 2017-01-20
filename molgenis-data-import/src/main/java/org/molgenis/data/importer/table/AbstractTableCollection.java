package org.molgenis.data.importer.table;

import java.util.stream.Stream;

public abstract class AbstractTableCollection implements TableCollection
{
	@Override
	public long getNrTables()
	{
		long nrTables;
		try (Stream<Table> tableStream = getTableStream())
		{
			nrTables = tableStream.count();
		}
		return nrTables;
	}
}
