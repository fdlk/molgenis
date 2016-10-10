package org.molgenis.data.discovery.job;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleCollectionMetaData;
import org.molgenis.data.discovery.meta.biobank.BiobankUniverseMetaData;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.service.BiobankUniverseService;
import org.molgenis.data.jobs.model.JobExecution;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.discovery.job.BiobankUniverseJobExecutionMetaData.MEMBERS;
import static org.molgenis.data.discovery.job.BiobankUniverseJobExecutionMetaData.UNIVERSE;

public class BiobankUniverseJobExecution extends JobExecution
{
	private final BiobankUniverseService biobankUniverseService;
	private final EntityManager entityManager;
	private final BiobankUniverseMetaData biobankUniverseMetaData;
	private final BiobankSampleCollectionMetaData biobankSampleCollectionMetaData;

	private static final long serialVersionUID = -1159893768494727598L;
	private static final String BIOBANK_UNIVERSE_JOB_TYPE = "BiobankUniverse";

	public BiobankUniverseJobExecution(BiobankUniverseJobExecutionMetaData biobankUniverseJobExecutionMetaData,
			BiobankUniverseMetaData biobankUniverseMetaData,
			BiobankSampleCollectionMetaData biobankSampleCollectionMetaData,
			BiobankUniverseService biobankUniverseService, EntityManager entityManager)
	{
		super(biobankUniverseJobExecutionMetaData);
		setType(BIOBANK_UNIVERSE_JOB_TYPE);
		this.biobankUniverseMetaData = requireNonNull(biobankUniverseMetaData);
		this.biobankSampleCollectionMetaData = requireNonNull(biobankSampleCollectionMetaData);
		this.biobankUniverseService = requireNonNull(biobankUniverseService);
		this.entityManager = requireNonNull(entityManager);
	}

	public BiobankUniverse getUniverse()
	{
		Entity biobankUniverseEntity = getEntity(UNIVERSE);
		return biobankUniverseService.getBiobankUniverse(biobankUniverseEntity.getIdValue().toString());
	}

	public void setUniverse(BiobankUniverse biobankUniverse)
	{
		Entity biobankUniverseEntity = entityManager
				.getReference(biobankUniverseMetaData, biobankUniverse.getIdentifier());
		set(BiobankUniverseJobExecutionMetaData.UNIVERSE, biobankUniverseEntity);
	}

	public List<BiobankSampleCollection> getMembers()
	{
		Iterable<Entity> entities = getEntities(MEMBERS);
		if (entities != null)
		{
			List<String> biobankSampleCollectionIds = StreamSupport.stream(entities.spliterator(), false)
					.map(Entity::getIdValue).map(Object::toString).collect(Collectors.toList());

			return biobankUniverseService.getBiobankSampleCollections(biobankSampleCollectionIds);
		}
		return Collections.emptyList();
	}

	public void setMembers(List<BiobankSampleCollection> members)
	{
		Iterable<Entity> biobankSampleCollectionEntities = entityManager.getReferences(biobankSampleCollectionMetaData,
				members.stream().map(BiobankSampleCollection::getName).collect(toList()));
		set(MEMBERS, biobankSampleCollectionEntities);
	}
}
