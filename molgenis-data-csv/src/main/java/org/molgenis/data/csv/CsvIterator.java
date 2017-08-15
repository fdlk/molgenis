package org.molgenis.data.csv;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.data.csv.CsvRepositoryCollection.MAC_ZIP;

import au.com.bytecode.opencsv.CSVReader;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.util.CloseableIterator;
import org.springframework.util.StringUtils;

public class CsvIterator implements CloseableIterator<Entity> {
  private final String repositoryName;
  private final EntityType entityType;
  private ZipFile zipFile;
  private CSVReader csvReader;
  private final List<CellProcessor> cellProcessors;
  private final Map<String, Integer> colNamesMap; // column names index
  private Entity next;
  private boolean getNext = true;
  private Character separator = null;

  CsvIterator(
      File file, String repositoryName, List<CellProcessor> cellProcessors, Character separator) {
    this(file, repositoryName, cellProcessors, separator, null);
  }

  CsvIterator(
      File file,
      String repositoryName,
      List<CellProcessor> cellProcessors,
      Character separator,
      EntityType entityType) {
    this.repositoryName = repositoryName;
    this.cellProcessors = cellProcessors;
    this.separator = separator;
    this.entityType = entityType;

    try {
      if (StringUtils.getFilenameExtension(file.getName()).equalsIgnoreCase("zip")) {
        zipFile = new ZipFile(file.getAbsolutePath());
        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
          ZipEntry entry = e.nextElement();
          if (!entry.getName().contains(MAC_ZIP) && !entry.isDirectory()) {
            String fileRepositoryName = FilenameUtils.getBaseName(entry.getName());
            if (fileRepositoryName.equalsIgnoreCase(repositoryName)) {
              csvReader = createCSVReader(entry.getName(), zipFile.getInputStream(entry));
              break;
            }
          }
        }
      } else if (file.getName().toLowerCase().startsWith(repositoryName.toLowerCase())) {
        csvReader = createCSVReader(file.getName(), new FileInputStream(file));
      }

      if (csvReader == null) {
        throw new UnknownEntityException("Unknown entity [" + repositoryName + "] ");
      }

      colNamesMap = toColNamesMap(csvReader.readNext());
    } catch (IOException e) {
      throw new MolgenisDataException("Exception reading [" + file.getAbsolutePath() + "]", e);
    }
  }

  Map<String, Integer> getColNamesMap() {
    return colNamesMap;
  }

  @Override
  public boolean hasNext() {
    boolean next = get() != null;
    if (!next) {
      close();
    }

    return next;
  }

  @Override
  public Entity next() {
    Entity entity = get();
    getNext = true;
    return entity;
  }

  private Entity get() {
    if (getNext) {
      try {
        String[] values = csvReader.readNext();

        if ((values != null) && (values.length >= colNamesMap.size())) {
          List<String> valueList = Arrays.asList(values);
          for (int i = 0; i < values.length; ++i) {
            // subsequent separators indicate
            // null
            // values instead of empty strings
            String value = values[i].isEmpty() ? null : values[i];
            values[i] = processCell(value, false);
          }

          next = new DynamicEntity(entityType);

          for (String name : colNamesMap.keySet()) {
            next.set(name, valueList.get(colNamesMap.get(name)));
          }
        } else {
          next = null;
        }

        getNext = false;
      } catch (IOException e) {
        throw new MolgenisDataException(
            "Exception reading line of csv file [" + repositoryName + "]", e);
      }
    }

    return next;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    IOUtils.closeQuietly(csvReader);

    if (zipFile != null) {
      IOUtils.closeQuietly(zipFile);
    }
  }

  private CSVReader createCSVReader(String fileName, InputStream in) {
    Reader reader = new InputStreamReader(in, UTF_8);

    if (null == separator) {
      if (fileName.toLowerCase().endsWith('.' + CsvFileExtensions.CSV.toString())
          || fileName.toLowerCase().endsWith('.' + CsvFileExtensions.TXT.toString())) {
        return new CSVReader(reader);
      }

      if (fileName.toLowerCase().endsWith('.' + CsvFileExtensions.TSV.toString())) {
        return new CSVReader(reader, '\t');
      }

      throw new MolgenisDataException("Unknown file type: [" + fileName + "] for csv repository");
    }

    return new CSVReader(reader, this.separator);
  }

  private Map<String, Integer> toColNamesMap(String[] headers) {
    if ((headers == null) || (headers.length == 0)) return Collections.emptyMap();

    int capacity = (int) (headers.length / 0.75) + 1;
    Map<String, Integer> columnIdx = new LinkedHashMap<>(capacity);
    for (int i = 0; i < headers.length; ++i) {
      String header = processCell(headers[i], true);
      columnIdx.put(header, i);
    }

    return columnIdx;
  }

  private String processCell(String value, boolean isHeader) {
    return AbstractCellProcessor.processCell(value, isHeader, cellProcessors);
  }
}
