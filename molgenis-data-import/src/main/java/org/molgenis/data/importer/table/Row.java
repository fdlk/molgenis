package org.molgenis.data.importer.table;

import java.util.List;

public interface Row
{
	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	String getValue(int i);

	/**
	 * @throws java.io.UncheckedIOException when an I/O exception occurred
	 */
	List<String> getValues();
}
