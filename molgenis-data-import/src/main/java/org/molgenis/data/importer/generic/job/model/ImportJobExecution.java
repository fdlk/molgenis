package org.molgenis.data.importer.generic.job.model;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.model.FileMeta;

public class ImportJobExecution extends JobExecution
{
	public ImportJobExecution(Entity entity)
	{
		super(entity);
		setDefaultValues();
	}

	public ImportJobExecution(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public ImportJobExecution(String id, EntityType entityType)
	{
		super(entityType);
		setIdentifier(id);
		setDefaultValues();
	}

	public FileMeta getFileMeta()
	{
		return getEntity(ImportJobExecutionMetadata.FILE_META, FileMeta.class);
	}

	public void setFileMeta(FileMeta fileMeta)
	{
		set(ImportJobExecutionMetadata.FILE_META, fileMeta);
	}

	private void setDefaultValues()
	{
		setType("import");
	}
}
