package org.molgenis.data.mapper.repository.impl;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.ALGORITHM;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.ALGORITHMSTATE;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.IDENTIFIER;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.SOURCEATTRIBUTEMETADATAS;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.TARGETATTRIBUTEMETADATA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.repository.AttributeMappingRepository;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class AttributeMappingRepositoryImpl implements AttributeMappingRepository
{
	private final IdGenerator idGenerator;
	private final DataService dataService;
	private final AttributeMappingMetaData attributeMappingMetaData;

	@Autowired
	public AttributeMappingRepositoryImpl(DataService dataService, IdGenerator idGenerator,
			AttributeMappingMetaData attributeMappingMetaData)
	{
		this.dataService = requireNonNull(dataService);
		this.idGenerator = requireNonNull(idGenerator);
		this.attributeMappingMetaData = requireNonNull(attributeMappingMetaData);
	}

	@Override
	public List<Entity> upsert(Collection<AttributeMapping> attributeMappings)
	{
		List<Entity> result = new ArrayList<Entity>();
		List<Entity> mappingEntitiesToAdd = new ArrayList<>();
		List<Entity> mappingEntitiesToUpdate = new ArrayList<>();

		for (AttributeMapping attributeMapping : attributeMappings)
		{
			Entity entity;
			if (StringUtils.isBlank(attributeMapping.getIdentifier()))
			{
				attributeMapping.setIdentifier(idGenerator.generateId());
				entity = toAttributeMappingEntity(attributeMapping);
				mappingEntitiesToAdd.add(entity);
			}
			else
			{
				entity = toAttributeMappingEntity(attributeMapping);
				mappingEntitiesToUpdate.add(entity);
			}
			result.add(entity);
		}

		if (mappingEntitiesToAdd.size() > 0)
		{
			dataService.add(attributeMappingMetaData.getName(), mappingEntitiesToAdd.stream());
		}

		if (mappingEntitiesToUpdate.size() > 0)
		{
			dataService.update(attributeMappingMetaData.getName(), mappingEntitiesToUpdate.stream());
		}

		return result;
	}

	@Override
	public List<AttributeMapping> getAttributeMappings(List<Entity> attributeMappingEntities,
			EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData)
	{
		return Lists.transform(attributeMappingEntities, new Function<Entity, AttributeMapping>()
		{
			@Override
			public AttributeMapping apply(Entity attributeMappingEntity)
			{
				return toAttributeMapping(attributeMappingEntity, sourceEntityMetaData, targetEntityMetaData);
			}
		});

	}

	@Override
	public List<AttributeMetaData> retrieveAttributeMetaDatasFromAlgorithm(String algorithm,
			EntityMetaData sourceEntityMetaData)
	{
		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();

		Pattern pattern = Pattern.compile("\\$\\('([^']+)'\\)");
		Matcher matcher = pattern.matcher(algorithm);

		while (matcher.find())
		{
			AttributeMetaData attribute = sourceEntityMetaData.getAttribute(matcher.group(1));
			if (!sourceAttributeMetaDatas.contains(attribute))
			{
				sourceAttributeMetaDatas.add(attribute);
			}
		}

		return sourceAttributeMetaDatas;
	}

	private AttributeMapping toAttributeMapping(Entity attributeMappingEntity, EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData)
	{
		String identifier = attributeMappingEntity.getString(IDENTIFIER);
		String targetAtributeName = attributeMappingEntity.getString(TARGETATTRIBUTEMETADATA);
		AttributeMetaData targetAttributeMetaData = targetEntityMetaData.getAttribute(targetAtributeName);
		String algorithm = attributeMappingEntity.getString(ALGORITHM);
		String algorithmState = attributeMappingEntity.getString(ALGORITHMSTATE);
		List<AttributeMetaData> sourceAttributeMetaDatas = retrieveAttributeMetaDatasFromAlgorithm(algorithm,
				sourceEntityMetaData);

		return new AttributeMapping(identifier, targetAttributeMetaData, algorithm, sourceAttributeMetaDatas,
				algorithmState);
	}

	private Entity toAttributeMappingEntity(AttributeMapping attributeMapping)
	{
		Entity attributeMappingEntity = new DynamicEntity(attributeMappingMetaData);
		attributeMappingEntity.set(IDENTIFIER, attributeMapping.getIdentifier());
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, attributeMapping.getTargetAttributeMetaData() != null
				? attributeMapping.getTargetAttributeMetaData().getName() : null);
		attributeMappingEntity.set(ALGORITHM, attributeMapping.getAlgorithm());
		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS, attributeMapping.getSourceAttributeMetaDatas().stream()
				.map(AttributeMetaData::getName).collect(Collectors.joining(",")));
		attributeMappingEntity.set(ALGORITHMSTATE, attributeMapping.getAlgorithmState().toString());
		return attributeMappingEntity;
	}

	@Override
	public void delete(List<AttributeMapping> attributeMappings)
	{
		if (attributeMappings.size() > 0)
		{
			Stream<Entity> stream = StreamSupport.stream(attributeMappings.spliterator(), false)
					.map(this::toAttributeMappingEntity);
			dataService.delete(attributeMappingMetaData.getName(), stream);
		}
	}
}
