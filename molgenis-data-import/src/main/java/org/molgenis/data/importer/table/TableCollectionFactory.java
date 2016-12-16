package org.molgenis.data.importer.table;

import org.molgenis.file.model.FileMeta;

import java.nio.file.Path;

public interface TableCollectionFactory
{
	TableCollection createTableCollection(Path path);

	TableCollection createTableCollection(Path path, FileMeta fileMeta);
}
