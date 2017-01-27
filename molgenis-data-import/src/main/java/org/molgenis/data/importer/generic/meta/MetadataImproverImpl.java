package org.molgenis.data.importer.generic.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

// TODO cleanup and improve
@Component
public class MetadataImproverImpl implements MetadataImprover
{
	private final DataService dataService;

	@Autowired
	public MetadataImproverImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public List<EntityType> improveMetadata(List<EntityType> entityTypes)
	{
		return entityTypes.stream().map(this::improveMetadata).collect(toList());
	}

	// TODO create new updated entity type instead of updating existing entity type
	private EntityType improveMetadata(EntityType entityType)
	{
		AttributeTypesReport attributeTypesReport = determineAttributeTypes(entityType);

		AtomicBoolean metadataUpdated = new AtomicBoolean(false);
		entityType.getOwnAllAttributes().forEach(attr ->
		{
			if (!attr.isIdAttribute())
			{
				AttributeTypeReport typeReport = attributeTypesReport.getTypeReport(attr);
				if (typeReport.isBoolean())
				{
					attr.setDataType(AttributeType.BOOL);
					metadataUpdated.set(true);
				}
				else if (typeReport.isLong())
				{
					attr.setDataType(AttributeType.LONG);
					metadataUpdated.set(true);
				}
			}
		});

		if (metadataUpdated.get())
		{
			dataService.getMeta().updateEntityType(entityType);
		}
		return entityType;
	}

	private AttributeTypesReport determineAttributeTypes(EntityType entityType)
	{
		AttributeTypesReport attributeTypesReport = new AttributeTypesReport(entityType);

		dataService.getRepository(entityType.getName()).forEachBatched(entities ->
		{
			entityType.getOwnAllAttributes().forEach(attr ->
			{
				if (!attr.isIdAttribute())
				{
					entities.forEach(entity ->
					{
						String strValue = entity.getString(attr.getName());
						if (strValue != null)
						{
							// check boolean
							if (attributeTypesReport.isBoolean(attr))
							{
								if (!(strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("false")))
								{
									attributeTypesReport.setBoolean(attr, false);
								}
							}

							// check long
							if (attributeTypesReport.isLong(attr))
							{
								try
								{
									Long.valueOf(strValue);
								}
								catch (NumberFormatException e)
								{
									attributeTypesReport.setLong(attr, false);
								}
							}
						}
					});
				}
			});
		}, 1000);
		return attributeTypesReport;
	}

	private static class AttributeTypesReport
	{
		private Map<String, AttributeTypeReport> attributeTypeReportMap;

		AttributeTypesReport(EntityType entityType)
		{
			attributeTypeReportMap = new HashMap<>();
			entityType.getOwnAllAttributes().forEach(attr ->
			{
				attributeTypeReportMap.put(attr.getName(), new AttributeTypeReport());
			});
		}

		void setBoolean(Attribute attr, boolean isBoolean)
		{
			attributeTypeReportMap.get(attr.getName()).setBoolean(isBoolean);
		}

		boolean isBoolean(Attribute attr)
		{
			return attributeTypeReportMap.get(attr.getName()).isBoolean();
		}

		boolean isLong(Attribute attr)
		{
			return attributeTypeReportMap.get(attr.getName()).isLong();
		}

		void setLong(Attribute attr, boolean isLong)
		{
			attributeTypeReportMap.get(attr.getName()).setLong(isLong);
		}

		public AttributeTypeReport getTypeReport(Attribute attr)
		{
			return attributeTypeReportMap.get(attr.getName());
		}
	}

	private static class AttributeTypeReport
	{
		private boolean isBoolean = true;
		private boolean isLong = true;

		public void setBoolean(boolean isBoolean)
		{
			this.isBoolean = isBoolean;
		}

		public boolean isBoolean()
		{
			return isBoolean;
		}

		public boolean isLong()
		{
			return isLong;
		}

		public void setLong(boolean isLong)
		{
			this.isLong = isLong;
		}
	}
}
