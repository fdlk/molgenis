package org.molgenis.data.importer.generic;

import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.file.FileContainer;
import org.molgenis.data.importer.table.Row;
import org.molgenis.data.importer.table.Table;
import org.molgenis.data.importer.table.TableCollection;
import org.molgenis.data.importer.table.TableCollectionFactory;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;

@Service
public class GenericImporterServiceImpl implements GenericImporterService
{
	private final DataService dataService;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attributeFactory;
	private final TableCollectionFactory tableCollectionFactory;

	@Autowired
	public GenericImporterServiceImpl(DataService dataService, EntityTypeFactory entityTypeFactory,
			AttributeFactory attributeFactory, TableCollectionFactory tableCollectionFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attributeFactory = requireNonNull(attributeFactory);
		this.tableCollectionFactory = requireNonNull(tableCollectionFactory);
	}

	@Transactional
	@Override
	public List<EntityType> importFile(FileContainer fileContainer)
	{
		List<EntityType> entityTypes = new ArrayList<>();
		TableCollection tableCollection = tableCollectionFactory
				.createTableCollection(fileContainer.getFilePath(), fileContainer.getFileMeta());
		try (Stream<Table> tableStream = tableCollection.getTableStream())
		{
			tableStream.forEach(table ->
			{
				List<String> headers = table.getHeaders();
				EntityType entityType = createMetadata(table);
				try (Repository<Entity> repository = dataService.getMeta().createRepository(entityType))
				{
					try (Stream<Row> rowStream = table.getRowStream())
					{
						repository.add(rowStream.map(row -> toEntity(row, headers, entityType)));
					}
				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}
				entityTypes.add(entityType);
			});
		}
		return entityTypes;
	}

	private Entity toEntity(Row row, List<String> headers, EntityType entityType)
	{
		Entity entity = new DynamicEntity(entityType);
		for (int i = 0; i < headers.size(); i++)
		{
			String attrName = headers.get(i);
			String strVal = row.getValue(i);
			Object val;
			Attribute attr = entityType.getAttribute(attrName);
			switch (attr.getDataType())
			{
				case BOOL:
					val = strVal != null ? Boolean.valueOf(strVal) : null;
					break;
				case LONG:
					val = strVal != null ? Long.valueOf(strVal) : null;
					break;
				default:
					val = strVal;
			}
			entity.set(attrName, val);
		}
		return entity;
	}

	private EntityType createMetadata(Table table)
	{
		List<Row> sampleRows;
		try (Stream<Row> rowStream = table.getRowStream())
		{
			sampleRows = rowStream.limit(100).collect(toList());
		}

		List<String> headers = table.getHeaders();
		String entityName = "entity" + System.nanoTime();
		EntityType entityType = entityTypeFactory.create();
		entityType.setLabel(table.getName());
		entityType.setSimpleName(entityName);
		for (int i = 0; i < headers.size(); i++)
		{
			Attribute attribute = attributeFactory.create();
			attribute.setName(headers.get(i));
			attribute.setNillable(determineNillable(i, sampleRows));
			attribute.setUnique(determineUnique(i, sampleRows));
			attribute.setSequenceNumber(i);
			if (i == 0)
			{
				attribute.setDataType(STRING);
				entityType.addAttribute(attribute, ROLE_ID);
			}
			else
			{
				attribute.setDataType(determineAttributeType(i, sampleRows));
				entityType.addAttribute(attribute);
			}
		}
		return entityType;
	}

	private boolean determineNillable(int i, List<Row> sampleRows)
	{
		return sampleRows.stream().anyMatch(row -> row.getValue(i) == null);
	}

	private boolean determineUnique(int i, List<Row> sampleRows)
	{
		Set<String> values = Sets.newHashSetWithExpectedSize(sampleRows.size());
		for (Row row : sampleRows)
		{
			boolean uniqueValue = values.add(row.getValue(i));
			if (!uniqueValue)
			{
				return false;
			}
		}
		return true;
	}

	private AttributeType determineAttributeType(int i, List<Row> sampleRows)
	{
		// bool
		int nrBool = 0;
		for (Row row : sampleRows)
		{
			String value = row.getValue(i);
			if (value != null)
			{
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
				{
					++nrBool;
				}
			}
		}
		if (nrBool > 0)
		{
			return AttributeType.BOOL;
		}

		// long
		int nrLong = 0;
		for (Row row : sampleRows)
		{
			String value = row.getValue(i);
			if (value != null)
			{
				try
				{
					Long.valueOf(value);
					++nrLong;
				}
				catch (NumberFormatException e)
				{
					nrLong = 0;
					break;
				}
			}
		}
		if (nrLong > 0)
		{
			return AttributeType.LONG;
		}

		return AttributeType.TEXT;
	}
}
