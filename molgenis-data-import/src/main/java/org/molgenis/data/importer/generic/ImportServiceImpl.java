package org.molgenis.data.importer.generic;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.importer.generic.mapper.MappedEntityType;
import org.molgenis.data.importer.generic.mapper.RowToEntityMapper;
import org.molgenis.data.importer.generic.mapper.TableToEntityTypeMapper;
import org.molgenis.data.importer.table.Row;
import org.molgenis.data.importer.table.Table;
import org.molgenis.data.importer.table.TableCollection;
import org.molgenis.data.importer.table.TableCollectionFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;

@Component
public class ImportServiceImpl implements ImportService
{
	private final FileStore fileStore;
	private final DataService dataService;
	private final TableCollectionFactory tableCollectionFactory;
	private final TableToEntityTypeMapper tableToEntityTypeMapper;
	private final RowToEntityMapper rowToEntityMapper;

	@Autowired
	public ImportServiceImpl(FileStore fileStore, DataService dataService,
			TableCollectionFactory tableCollectionFactory, TableToEntityTypeMapper tableToEntityTypeMapper,
			RowToEntityMapper rowToEntityMapper)
	{
		this.fileStore = requireNonNull(fileStore);
		this.dataService = requireNonNull(dataService);
		this.tableCollectionFactory = requireNonNull(tableCollectionFactory);
		this.tableToEntityTypeMapper = requireNonNull(tableToEntityTypeMapper);
		this.rowToEntityMapper = requireNonNull(rowToEntityMapper);
	}

	@Transactional
	@Override
	public ImportResult importFile(FileMeta fileMeta, Progress progress)
	{
		File file = fileStore.getFile(fileMeta.getId());
		TableCollection tableCollection = tableCollectionFactory.createTableCollection(file.toPath(), fileMeta);
		progress.setProgressMax(toIntExact(tableCollection.getNrTables()));

		List<EntityType> entityTypes = new ArrayList<>();
		try (Stream<Table> tableStream = tableCollection.getTableStream())
		{
			AtomicInteger progressIdx = new AtomicInteger(0);
			tableStream.forEach(table ->
			{
				progress.progress(progressIdx.getAndIncrement(), "Importing " + table.getName() + "...");
				EntityType entityType = importTable(table);
				entityTypes.add(entityType);
			});
		}
		return ImportResult.create(entityTypes);
	}

	private EntityType importTable(Table table)
	{
		MappedEntityType mappedEntityType = tableToEntityTypeMapper.create(table);

		EntityType entityType = mappedEntityType.getEntityType();
		try (Repository<Entity> repository = dataService.getMeta().createRepository(entityType))
		{
			try (Stream<Row> rowStream = table.getRowStream())
			{
				repository.add(rowStream.map(row -> rowToEntityMapper.create(row, mappedEntityType)));
			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return entityType;
	}
}
