package org.molgenis.data.importer.generic.job.model;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.system.model.RootSystemPackage;
import org.molgenis.file.model.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class ImportJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "ImportJobExecution";
	public static final String IMPORT_JOB_EXECUTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;
	private final JobExecutionMetaData jobExecutionMetaData;
	private final FileMetaMetaData fileMetaMetadata;

	public static final String FILE_META = "fileMeta";

	@Autowired
	ImportJobExecutionMetadata(RootSystemPackage rootSystemPackage, JobExecutionMetaData jobExecutionMetaData,
			FileMetaMetaData fileMetaMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.fileMetaMetadata = requireNonNull(fileMetaMetadata);
	}

	@Override
	public void init()
	{
		setLabel("Import job execution");
		setPackage(rootSystemPackage);
		setExtends(jobExecutionMetaData);

		addAttribute(FILE_META).setDataType(XREF).setRefEntity(fileMetaMetadata).setNillable(false)
				.setLabel("File metadata");
	}
}
