package org.molgenis.data.importer.generic.job;

import org.molgenis.data.DataService;
import org.molgenis.data.importer.generic.ImportService;
import org.molgenis.data.importer.generic.job.model.ImportJobExecution;
import org.molgenis.data.importer.generic.job.model.ImportJobExecutionFactory;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.file.model.FileMeta;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.importer.generic.job.model.ImportJobExecutionMetadata.IMPORT_JOB_EXECUTION;

@Component
public class ImportJobFactory
{
	private final DataService dataService;
	private final ImportJobExecutionFactory importJobExecutionFactory;
	private final ImportService importService;
	private final JobExecutionUpdater jobExecutionUpdater;
	private final MailSender mailSender;
	private final PlatformTransactionManager transactionManager;

	@Autowired
	public ImportJobFactory(DataService dataService, ImportJobExecutionFactory importJobExecutionFactory,
			ImportService importService, JobExecutionUpdater jobExecutionUpdater, MailSender mailSender,
			PlatformTransactionManager transactionManager)
	{
		this.dataService = requireNonNull(dataService);
		this.importJobExecutionFactory = requireNonNull(importJobExecutionFactory);
		this.importService = requireNonNull(importService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.mailSender = requireNonNull(mailSender);
		this.transactionManager = requireNonNull(transactionManager);
	}

	ImportJob createImportJob(FileMeta fileMeta)
	{
		ImportJobExecution importJobExecution = createImportJobExecution(fileMeta);
		dataService.add(IMPORT_JOB_EXECUTION, importJobExecution);
		return createImportJob(importJobExecution);
	}

	private ImportJobExecution createImportJobExecution(FileMeta fileMeta)
	{
		ImportJobExecution importJobExecution = importJobExecutionFactory.create();
		importJobExecution.setType("importer");
		importJobExecution.setUser(SecurityUtils.getCurrentUsername());
		importJobExecution.setFileMeta(fileMeta);
		return importJobExecution;
	}

	private ImportJob createImportJob(ImportJobExecution importJobExecution)
	{
		Progress progress = new ProgressImpl(importJobExecution, jobExecutionUpdater, mailSender);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return new ImportJob(progress, transactionTemplate, authentication, importService, importJobExecution,
				dataService);
	}
}
