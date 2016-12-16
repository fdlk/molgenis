package org.molgenis.data.importer.table.csv;

import org.molgenis.data.importer.table.Row;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class CsvRow implements Row
{
	private final String[] tokens;

	CsvRow(String[] tokens)
	{
		this.tokens = requireNonNull(tokens);
	}

	@Override
	public String getValue(int i)
	{
		return tokens[i];
	}

	@Override
	public List<String> getValues()
	{
		return Collections.unmodifiableList(asList(tokens));
	}
}
