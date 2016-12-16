package org.molgenis.data.importer.table.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.molgenis.data.importer.table.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

public class ExcelRow implements Row
{
	private final org.apache.poi.ss.usermodel.Row row;
	private final List<String> headers;

	ExcelRow(org.apache.poi.ss.usermodel.Row row, List<String> headers)
	{
		this.row = requireNonNull(row);
		this.headers = requireNonNull(headers);
	}

	@Override
	public String getValue(int i)
	{
		if (i >= headers.size())
		{
			throw new NoSuchElementException();
		}

		Cell cell = row.getCell(i);
		return cell != null ? ExcelCellUtils.getStringCellValue(cell) : null;
	}

	@Override
	public List<String> getValues()
	{
		List<String> values = new ArrayList<>(headers.size());
		for (int i = 0; i < headers.size(); ++i)
		{
			Cell cell = row.getCell(i);
			String strValue = cell != null ? ExcelCellUtils.getStringCellValue(cell) : null;
			values.add(strValue);
		}
		return values;
	}
}
