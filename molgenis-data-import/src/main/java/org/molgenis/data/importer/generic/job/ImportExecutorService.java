package org.molgenis.data.importer.generic.job;

import org.molgenis.data.importer.generic.job.model.ImportJobExecution;
import org.molgenis.file.model.FileMeta;

public interface ImportExecutorService
{
	ImportJobExecution executeImport(FileMeta fileMeta);
}
