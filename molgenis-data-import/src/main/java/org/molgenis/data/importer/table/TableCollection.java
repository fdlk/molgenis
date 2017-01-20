package org.molgenis.data.importer.table;

import java.util.stream.Stream;

public interface TableCollection
{
	long getNrTables();

	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	Stream<Table> getTableStream();
}
