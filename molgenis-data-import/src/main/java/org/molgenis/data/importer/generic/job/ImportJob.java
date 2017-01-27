package org.molgenis.data.importer.generic.job;

import org.molgenis.data.DataService;
import org.molgenis.data.importer.generic.ImportResult;
import org.molgenis.data.importer.generic.ImportService;
import org.molgenis.data.importer.generic.job.model.ImportJobExecution;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.importer.generic.job.model.ImportJobExecutionMetadata.IMPORT_JOB_EXECUTION;

public class ImportJob extends Job<ImportResult>
{
	private final ImportService importService;
	private final ImportJobExecution importJobExecution;
	private final DataService dataService;

	public ImportJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			ImportService importService, ImportJobExecution importJobExecution, DataService dataService)
	{
		super(progress, transactionTemplate, authentication);
		this.importService = requireNonNull(importService);
		this.importJobExecution = requireNonNull(importJobExecution);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public ImportResult call(Progress progress) throws Exception
	{
		ImportResult importResult = importService.importFile(importJobExecution.getFileMeta(), progress);
		updateImportJobExecution(importResult);
		return importResult;
	}

	public ImportJobExecution getImportJobExecution()
	{
		return importJobExecution;
	}

	private void updateImportJobExecution(ImportResult importResult)
	{
		String entityTypesStr = importResult.getEntityTypes().stream()
				.map(entityType -> entityType.getIdValue().toString() + ':' + entityType.getLabel().toString())
				.collect(joining(","));
		importJobExecution.setEntityTypes(entityTypesStr);
		dataService.update(IMPORT_JOB_EXECUTION, importJobExecution);
	}
}
