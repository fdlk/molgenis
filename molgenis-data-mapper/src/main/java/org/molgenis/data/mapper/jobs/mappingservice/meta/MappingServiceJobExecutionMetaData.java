package org.molgenis.data.mapper.jobs.mappingservice.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.mapper.meta.MapperPackage;
import org.molgenis.data.mapper.meta.MappingProjectMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappingServiceJobExecutionMetaData extends SystemEntityMetaData
{
	private final MapperPackage mapperPackage;
	private final JobExecutionMetaData jobExecutionMetaData;
	private final MappingProjectMetaData mappingProjectMetaData;
	private final EntityMetaDataMetaData entityMetaDataMetaData;

	public static final String SIMPLE_NAME = "MappingServiceJobExecution";
	public static final String MAPPING_SERVICE_JOB_EXECUTION = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String MAPPING_PROJECT = "mappingProject";
	public static final String TARGET_ENTITY = "targetEntity";
	public static final String SOURCE_ENTITIES = "sourceEntities";
	public static final String JOB_TYPE = "MappingService";

	@Autowired
	MappingServiceJobExecutionMetaData(MapperPackage mapperPackage, JobExecutionMetaData jobExecutionMetaData,
			MappingProjectMetaData mappingProjectMetaData, EntityMetaDataMetaData entityMetaDataMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.mappingProjectMetaData = requireNonNull(mappingProjectMetaData);
		this.entityMetaDataMetaData = requireNonNull(entityMetaDataMetaData);
	}

	@Override
	public void init()
	{
		setLabel("MappingService job execution");
		setPackage(mapperPackage);

		setExtends(jobExecutionMetaData);
		addAttribute(MAPPING_PROJECT).setDataType(XREF).setLabel("Mapping Project").setNillable(false)
				.setRefEntity(mappingProjectMetaData);
		addAttribute(TARGET_ENTITY).setDataType(XREF).setLabel("Target").setNillable(false)
				.setRefEntity(entityMetaDataMetaData);
		addAttribute(SOURCE_ENTITIES).setDataType(MREF).setLabel("Added source").setNillable(false)
				.setRefEntity(entityMetaDataMetaData);

	}
}