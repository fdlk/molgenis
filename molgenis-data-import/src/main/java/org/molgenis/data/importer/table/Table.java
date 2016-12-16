package org.molgenis.data.importer.table;

import java.util.List;
import java.util.stream.Stream;

public interface Table
{
	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	String getName();

	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	List<String> getHeaders();

	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	Stream<Row> getRowStream();
}
