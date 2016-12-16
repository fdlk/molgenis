package org.molgenis.data.importer.table.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.lang.String.format;

class ExcelCellUtils
{
	private ExcelCellUtils()
	{
	}

	static String getStringCellValue(Cell cell)
	{
		String value;
		int cellType = cell.getCellType();
		switch (cellType)
		{
			case Cell.CELL_TYPE_BLANK:
				value = null;
				break;
			case Cell.CELL_TYPE_NUMERIC:
				// excel stores integer values as double values
				// read an integer if the double value equals the
				// integer value
				double x = cell.getNumericCellValue();
				if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x))
				{
					value = String.valueOf((int) x);
				}
				else
				{
					value = String.valueOf(x);
				}
				break;
			case Cell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_FORMULA:
				FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
				CellValue cellValue = evaluator.evaluate(cell);
				if (cellValue.getCellType() == Cell.CELL_TYPE_ERROR)
				{
					throw new UncheckedIOException(new IOException(
							format("Error evaluating formula in excel sheet [%s] row [%d] column [%d]",
									cell.getSheet().getSheetName(), cell.getRowIndex() + 1,
									cell.getColumnIndex() + 1)));
				}
				return getStringCellValue(cellValue);
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case Cell.CELL_TYPE_ERROR:
				throw new UncheckedIOException(new IOException(
						format("Excel sheet [%s] row [%d] column [%d] contains an error",
								cell.getSheet().getSheetName(), cell.getRowIndex() + 1, cell.getColumnIndex() + 1)));
			default:
				throw new RuntimeException(format("Unknown Excel cell type [%d]", cellType));

		}
		return value;
	}

	private static String getStringCellValue(CellValue cellValue)
	{
		String value;
		int cellType = cellValue.getCellType();
		switch (cellType)
		{
			case Cell.CELL_TYPE_BLANK:
				value = null;
				break;
			case Cell.CELL_TYPE_NUMERIC:
				// excel stores integer values as double values
				// read an integer if the double value equals the
				// integer value
				double x = cellValue.getNumberValue();
				if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x))
				{
					value = String.valueOf((int) x);
				}
				else
				{
					value = String.valueOf(x);
				}
				break;
			case Cell.CELL_TYPE_STRING:
				value = cellValue.getStringValue();
				break;
			case Cell.CELL_TYPE_FORMULA:
			case Cell.CELL_TYPE_ERROR:
				throw new RuntimeException(format("Invalid Excel cell type [%d]", cellType));
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(cellValue.getBooleanValue());
			default:
				throw new RuntimeException(format("Unknown Excel cell type [%d]", cellType));
		}
		return value;
	}
}
