package org.molgenis.data.importer.generic.job.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportJobExecutionFactory
		extends AbstractSystemEntityFactory<ImportJobExecution, ImportJobExecutionMetadata, String>
{
	@Autowired
	ImportJobExecutionFactory(ImportJobExecutionMetadata importJobExecutionMetadata, EntityPopulator entityPopulator)
	{
		super(ImportJobExecution.class, importJobExecutionMetadata, entityPopulator);
	}
}
