package org.molgenis.data.mapper.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.js.EvaluationResult;
import org.molgenis.js.RhinoConfig;
import org.molgenis.js.ScriptEvaluator;
import org.molgenis.security.core.runas.RunAsSystem;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class AlgorithmServiceImpl implements AlgorithmService
{
	private static final Logger LOG = LoggerFactory.getLogger(AlgorithmServiceImpl.class);

	@Autowired
	private DataService dataService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	public AlgorithmServiceImpl()
	{
		new RhinoConfig().init();
	}

	@Override
	@RunAsSystem
	public void autoGenerateAlgorithm(EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			EntityMapping mapping, AttributeMetaData targetAttribute)
	{
		LOG.debug("createAttributeMappingIfOnlyOneMatch: target= " + targetAttribute.getName());
		Iterable<AttributeMetaData> matches = semanticSearchService.findAttributes(sourceEntityMetaData,
				targetEntityMetaData, targetAttribute);
		if (Iterables.size(matches) == 1)
		{
			AttributeMetaData source = matches.iterator().next();
			AttributeMapping attributeMapping = mapping.addAttributeMapping(targetAttribute.getName());
			String algorithm = "$('" + source.getName() + "').value();";
			attributeMapping.setAlgorithm(algorithm);
			LOG.info("Creating attribute mapping: " + targetAttribute.getName() + " = " + algorithm);
		}
	}

	@Override
	public Iterable<EvaluationResult> applyAlgorithm(AttributeMetaData targetAttribute, String algorithm,
			Iterable<Entity> sourceEntities)
	{
		Iterable<Entity> mapEntities = Iterables.transform(sourceEntities,
				entity -> createMapEntity(getSourceAttributeNames(algorithm), entity));
		return magmaScriptEvaluator.eval(algorithm, mapEntities, Iterables.get(sourceEntities, 0).getEntityMetaData());
	}

	private MapEntity createMapEntity(Collection<String> attributeNames, Entity entity)
	{
		MapEntity mapEntity = new MapEntity();
		for (String attributeName : attributeNames)
		{
			Object value = entity.get(attributeName);
			if (value instanceof Entity)
			{
				value = ((Entity) value).getIdValue();
			}
			mapEntity.set(attributeName, value);
		}
		return mapEntity;
	}

	@Override
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityMetaData sourceEntityMetaData)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (StringUtils.isEmpty(algorithm))
		{
			return null;
		}

		MapEntity entity = createMapEntity(getSourceAttributeNames(attributeMapping.getAlgorithm()), sourceEntity);
		Object value = ScriptEvaluator.eval(algorithm, entity, sourceEntityMetaData);
		return convert(value, attributeMapping.getTargetAttributeMetaData());
	}

	private Object convert(Object value, AttributeMetaData attributeMetaData)
	{
		if (value == null)
		{
			return null;
		}
		Object convertedValue;
		FieldTypeEnum targetDataType = attributeMetaData.getDataType().getEnumType();
		switch (targetDataType)
		{
			case DATE:
			case DATE_TIME:
				convertedValue = Context.jsToJava(value, Date.class);
				break;
			case INT:
				convertedValue = Integer.parseInt(Context.toString(value));
				break;
			case DECIMAL:
				convertedValue = Context.toNumber(value);
				break;
			case XREF:
			case CATEGORICAL:
				convertedValue = dataService.findOne(attributeMetaData.getRefEntity().getName(),
						Context.toString(value));
				break;
			case MREF:
			case CATEGORICAL_MREF:
			{
				NativeArray mrefIds = (NativeArray) value;
				if (mrefIds != null && !mrefIds.isEmpty())
				{
					EntityMetaData refEntityMeta = attributeMetaData.getRefEntity();
					convertedValue = dataService.findAll(refEntityMeta.getName(), mrefIds);
				}
				else
				{
					convertedValue = null;
				}
				break;
			}
			default:
				convertedValue = Context.toString(value);
				break;
		}
		return convertedValue;
	}

	@Override
	public Collection<String> getSourceAttributeNames(String algorithmScript)
	{
		Collection<String> result = Collections.emptyList();
		if (!StringUtils.isEmpty(algorithmScript))
		{
			result = findMatchesForPattern(algorithmScript, "\\$\\('([^\\$\\(\\)]+)'\\)");
			if (result.isEmpty())
			{
				result = findMatchesForPattern(algorithmScript, "\\$\\(([^\\$\\(\\)]+)\\)");
			}
		}
		return result;
	}

	private static Collection<String> findMatchesForPattern(String algorithmScript, String patternString)
	{
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		Matcher matcher = Pattern.compile(patternString).matcher(algorithmScript);
		while (matcher.find())
		{
			result.add(matcher.group(1));
		}
		return result;
	}

}
