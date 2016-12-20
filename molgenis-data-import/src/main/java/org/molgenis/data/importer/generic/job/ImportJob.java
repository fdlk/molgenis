package org.molgenis.data.importer.generic.job;

import org.molgenis.data.importer.generic.ImportResult;
import org.molgenis.data.importer.generic.ImportService;
import org.molgenis.data.importer.generic.job.model.ImportJobExecution;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;

public class ImportJob extends Job<ImportResult>
{
	private final ImportService importService;
	private final ImportJobExecution importJobExecution;

	public ImportJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			ImportService importService, ImportJobExecution importJobExecution)
	{
		super(progress, transactionTemplate, authentication);
		this.importService = requireNonNull(importService);
		this.importJobExecution = requireNonNull(importJobExecution);
	}

	@Override
	public ImportResult call(Progress progress) throws Exception
	{
		return importService.importFile(importJobExecution.getFileMeta(), progress);
	}
}
