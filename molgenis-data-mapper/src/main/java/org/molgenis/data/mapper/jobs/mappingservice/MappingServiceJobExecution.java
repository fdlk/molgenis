package org.molgenis.data.mapper.jobs.mappingservice;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.jobs.mappingservice.meta.MappingServiceJobExecutionMetaData.JOB_TYPE;
import static org.molgenis.data.mapper.jobs.mappingservice.meta.MappingServiceJobExecutionMetaData.MAPPING_PROJECT;
import static org.molgenis.data.mapper.jobs.mappingservice.meta.MappingServiceJobExecutionMetaData.SOURCE_ENTITIES;
import static org.molgenis.data.mapper.jobs.mappingservice.meta.MappingServiceJobExecutionMetaData.TARGET_ENTITY;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.model.EntityMetaData;

import com.google.common.collect.Lists;

public class MappingServiceJobExecution extends JobExecution
{
	private static final long serialVersionUID = 1003043918322982403L;

	private final MappingService mappingService;

	public MappingServiceJobExecution(EntityMetaData entityMetaData, MappingService mappingService)
	{
		super(entityMetaData);
		setType(JOB_TYPE);
		this.mappingService = requireNonNull(mappingService);
	}

	public EntityMetaData getTargetEntity()
	{
		return getEntity(TARGET_ENTITY, EntityMetaData.class);
	}

	public void setTargetEntity(EntityMetaData targetEntityMetaData)
	{
		set(TARGET_ENTITY, targetEntityMetaData);
	}

	public List<EntityMetaData> getSourceEntities()
	{
		return Lists.newArrayList(getEntities(SOURCE_ENTITIES, EntityMetaData.class));
	}

	public void setSourceEntity(List<EntityMetaData> sourceEntityMetaDatas)
	{
		set(SOURCE_ENTITIES, sourceEntityMetaDatas);
	}

	public MappingProject getMappingProject()
	{
		Entity mappingProjectEntity = getEntity(MAPPING_PROJECT);
		return mappingService.getMappingProject(mappingProjectEntity.getIdValue().toString());
	}

	public void setMappingProject(Entity mappingProjectEntity)
	{
		set(MAPPING_PROJECT, mappingProjectEntity);
	}
}