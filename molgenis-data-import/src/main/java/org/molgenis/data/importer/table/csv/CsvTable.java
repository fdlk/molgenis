package org.molgenis.data.importer.table.csv;

import au.com.bytecode.opencsv.CSVReader;
import org.molgenis.data.importer.table.Row;
import org.molgenis.data.importer.table.Table;
import org.molgenis.file.model.FileMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class CsvTable implements Table
{
	private final Path path;
	private final FileMeta fileMeta;
	private transient List<String> headers;

	CsvTable(Path path)
	{
		this(path, null);
	}

	CsvTable(Path path, FileMeta fileMeta)
	{
		this.path = requireNonNull(path);
		this.fileMeta = fileMeta;
	}

	@Override
	public String getName()
	{
		return fileMeta != null ? fileMeta.getFilename() : path.getFileName().toString();
	}

	@Override
	public List<String> getHeaders()
	{
		synchronized (this)
		{
			if (headers == null)
			{
				try (CSVReader csvReader = createCsvReader())
				{
					String[] tokens = csvReader.readNext();
					if (tokens == null)
					{
						throw new RuntimeException(String.format("File [%s] is empty", path.getFileName().toString()));
					}
					headers = asList(tokens);
				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}
			}
		}
		return headers;
	}

	@Override
	public Stream<Row> getRowStream()
	{
		CSVReader csvReader = createCsvReader();
		try
		{
			csvReader.readNext(); // skip header row
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		Iterator<Row> csvRowIterator = new CsvRowIterator(csvReader);
		Runnable runnable = () ->
		{
			try
			{
				csvReader.close();
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
		return stream(spliteratorUnknownSize(csvRowIterator, ORDERED), false).onClose(runnable);
	}

	private CSVReader createCsvReader()
	{
		try
		{
			char separator = getSeparator();
			return new CSVReader(new InputStreamReader(Files.newInputStream(path), UTF_8), separator);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private char getSeparator()
	{
		String fileName = fileMeta != null ? fileMeta.getFilename() : path.getFileName().toString();
		return fileName.toLowerCase().endsWith(".tsv") ? '\t' : ',';
	}
}
