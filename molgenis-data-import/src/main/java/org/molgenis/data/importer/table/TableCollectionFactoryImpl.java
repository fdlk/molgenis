package org.molgenis.data.importer.table;

import org.molgenis.data.importer.table.csv.CsvTableCollection;
import org.molgenis.data.importer.table.excel.XlsTableCollection;
import org.molgenis.data.importer.table.excel.XlsxTableCollection;
import org.molgenis.data.importer.table.zip.ArchiveTableCollection;
import org.molgenis.file.model.FileMeta;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class TableCollectionFactoryImpl implements TableCollectionFactory
{
	@Override
	public TableCollection createTableCollection(Path path)
	{
		return createTableCollection(path, null);
	}

	@Override
	public TableCollection createTableCollection(Path path, FileMeta fileMeta)
	{
		String fileName = fileMeta != null ? fileMeta.getFilename() : path.getFileName().toString();
		String lowercaseFileName = fileName.toLowerCase();

		if (lowercaseFileName.endsWith(".xlsx"))
		{
			return new XlsxTableCollection(path);
		}
		else if (lowercaseFileName.endsWith(".xls"))
		{
			return new XlsTableCollection(path);
		}
		else if (lowercaseFileName.endsWith(".csv") || lowercaseFileName.endsWith(".tsv"))
		{
			return new CsvTableCollection(path, fileMeta);
		}
		else if (lowercaseFileName.endsWith(".zip"))
		{
			return new ArchiveTableCollection(path, this);
		}
		else
		{
			throw new InvalidTableCollectionException(fileName);
		}
	}
}
