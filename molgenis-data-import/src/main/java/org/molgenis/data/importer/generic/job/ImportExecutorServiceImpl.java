package org.molgenis.data.importer.generic.job;

import org.molgenis.data.importer.generic.ImportResult;
import org.molgenis.file.model.FileMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

@Service
public class ImportExecutorServiceImpl implements ImportExecutorService
{
	private final ExecutorService executorService;
	private final ImportJobFactory importJobFactory;

	@Autowired
	public ImportExecutorServiceImpl(ExecutorService executorService, ImportJobFactory importJobFactory)
	{
		this.executorService = requireNonNull(executorService);
		this.importJobFactory = requireNonNull(importJobFactory);
	}

	@Transactional
	@Override
	public Future<ImportResult> importFile(FileMeta fileMeta)
	{
		ImportJob importJob = importJobFactory.createImportJob(fileMeta);
		return executorService.submit(importJob);
	}
}
