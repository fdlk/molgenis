package org.molgenis.data.cache.l2.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.cache.l2.settings.L2CacheSettingsMetaData.CACHED_ENTITY;
import static org.molgenis.data.cache.l2.settings.L2CacheSettingsMetaData.L2CACHE_SETTINGS;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.util.EntityUtils.getTypedValue;

@Service
public class L2CacheSettingsService
{
	private final DataService dataService;
	private final L2CacheSettingsFactory l2CacheSettingsFactory;
	private final EntityManager entityManager;

	@Autowired
	public L2CacheSettingsService(L2CacheSettingsFactory l2CacheSettingsFactory, DataService dataService,
			EntityManager entityManager)
	{
		this.l2CacheSettingsFactory = requireNonNull(l2CacheSettingsFactory);
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
	}

	public L2CacheSettings getCacheSettings(EntityMetaData cachedEntity)
	{
		if (cachedEntity.getPackage().getName().startsWith(PACKAGE_META) || cachedEntity.getName()
				.equals(L2CACHE_SETTINGS))
		{
			// Too meta meta, use default settings instead
			return createDefaultSettings(cachedEntity);
		}
		L2CacheSettings existingCacheSettings = dataService.query(L2CACHE_SETTINGS, L2CacheSettings.class)
				.eq(CACHED_ENTITY, cachedEntity).findOne();
		if (existingCacheSettings != null)
		{
			return existingCacheSettings;
		}
		return createNewSettings(cachedEntity);
	}

	private synchronized L2CacheSettings createNewSettings(EntityMetaData cachedEntity)
	{
		// Just to be sure, check that the settings still don't exist
		L2CacheSettings result = dataService.query(L2CACHE_SETTINGS, L2CacheSettings.class)
				.eq(CACHED_ENTITY, cachedEntity).findOne();
		if (result == null)
		{
			result = createDefaultSettings(cachedEntity);
			dataService.add(L2CACHE_SETTINGS, result);
		}
		return result;
	}

	//TODO: Once default values also get set by the factory, remove this
	private L2CacheSettings createDefaultSettings(EntityMetaData cachedEntity)
	{
		L2CacheSettings result = l2CacheSettingsFactory.create();
		for (AttributeMetaData attr : result.getEntityMetaData().getAtomicAttributes())
		{
			// default values are stored/retrieved as strings, so we convert them to the required type here.
			String defaultValue = attr.getDefaultValue();
			if (defaultValue != null)
			{
				Object typedDefaultValue = getTypedValue(defaultValue, attr, entityManager);
				result.set(attr.getName(), typedDefaultValue);
			}
		}
		result.setCachedEntity(cachedEntity);
		return result;
	}

}
