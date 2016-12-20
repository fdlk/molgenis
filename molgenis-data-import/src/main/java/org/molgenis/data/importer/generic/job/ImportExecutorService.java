package org.molgenis.data.importer.generic.job;

import org.molgenis.data.importer.generic.ImportResult;
import org.molgenis.file.model.FileMeta;

import java.util.concurrent.Future;

public interface ImportExecutorService
{
	Future<ImportResult> importFile(FileMeta fileMeta);
}
