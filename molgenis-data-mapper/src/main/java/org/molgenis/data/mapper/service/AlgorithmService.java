package org.molgenis.data.mapper.service;

import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

public interface AlgorithmService
{
	List<Object> applyAlgorithm(AttributeMetaData targetAttribute, String algorithm, Repository sourceRepo);

	/**
	 * Applies an {@link AttributeMapping} to a source {@link Entity}
	 * 
	 * @param attributeMapping
	 *            {@link AttributeMapping} to apply
	 * @param sourceEntity
	 *            {@link Entity} to apply the mapping to
	 * @return Object containing the mapped value
	 */
	Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityMetaData sourceEntityMetaData);

	/**
	 * Retrieves the names of the source attributes in an algorithm
	 * 
	 * @param algorithmScript
	 *            String with the algorithm script
	 * @return Collection of source attribute name Strings
	 */
	Collection<String> getSourceAttributeNames(String algorithmScript);

	/**
	 * Starts a job to generate algorithms.
	 * 
	 * @param sourceEntityMetaData
	 * @param targetEntityMetaData
	 * @param mapping
	 * @param mappingService
	 * @return the jobInstanceId
	 * @throws JobParametersInvalidException
	 * @throws JobInstanceAlreadyCompleteException
	 * @throws JobRestartException
	 * @throws JobExecutionAlreadyRunningException
	 */
	long autoGenerateAlgorithmsAsync(EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			EntityMapping mapping, MappingProject project, MappingService mappingService)
			throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException,
			JobParametersInvalidException;

	/**
	 * Creates an attribute mapping after the semantic search service finds one
	 * 
	 * @param sourceEntityMetaData
	 * @param targetEntityMetaData
	 * @param mapping
	 * @param targetAttribute
	 */
	void autoGenerateAlgorithm(EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			EntityMapping mapping, AttributeMetaData targetAttribute);
}
