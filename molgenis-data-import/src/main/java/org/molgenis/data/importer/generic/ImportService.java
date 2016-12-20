package org.molgenis.data.importer.generic;

import org.molgenis.data.jobs.Progress;
import org.molgenis.file.model.FileMeta;

public interface ImportService
{
	ImportResult importFile(FileMeta fileMeta, Progress progress);
}
