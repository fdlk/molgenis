package org.molgenis.data.importer.table.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.importer.table.Row;
import org.molgenis.data.importer.table.Table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class ExcelTable implements Table
{
	private final Sheet sheet;
	private transient List<String> headers;

	ExcelTable(Sheet sheet)
	{
		this.sheet = requireNonNull(sheet);
	}

	@Override
	public String getName()
	{
		return sheet.getSheetName();
	}

	@Override
	public List<String> getHeaders()
	{
		synchronized (this)
		{
			if (headers == null)
			{
				Iterator<org.apache.poi.ss.usermodel.Row> it = sheet.iterator();
				if (!it.hasNext())
				{
					throw new RuntimeException(format("Workbook sheet [%s] is empty", sheet.getSheetName()));
				}
				headers = createHeaders(it.next());
			}
		}
		return headers;
	}

	@Override
	public Stream<Row> getRowStream()
	{
		Iterator<org.apache.poi.ss.usermodel.Row> sheetRowIterator = sheet.iterator();
		if (!sheetRowIterator.hasNext())
		{
			return Stream.empty();
		}

		sheetRowIterator.next(); // skip header row

		List<String> headers = getHeaders();
		return stream(spliteratorUnknownSize(sheetRowIterator, ORDERED), false).map(row -> new ExcelRow(row, headers));
	}

	private List<String> createHeaders(org.apache.poi.ss.usermodel.Row row)
	{
		short lastCellNum = row.getLastCellNum();
		if (lastCellNum == -1)
		{
			throw new RuntimeException(format("Workbook sheet [%s] is empty", sheet.getSheetName()));
		}

		List<String> headers = new ArrayList<>(lastCellNum);
		for (short i = 0; i < lastCellNum; ++i)
		{
			Cell cell = row.getCell(i);
			if (cell == null)
			{
				// TODO translate 0 -> A, 1-> B, 2 -> C etc.
				throw new RuntimeException(
						format("Workbook sheet [%s] is missing header in column [%d]", sheet.getSheetName(), i));
			}

			String stringCellValue = ExcelCellUtils.getStringCellValue(cell);
			headers.add(stringCellValue);
		}
		return headers;
	}

}
