package org.molgenis.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.io.File.separator;
import static java.net.URLConnection.guessContentTypeFromName;
import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_RANDOM;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;

public class FileStoreImpl implements FileStore
{
	private final String storageDir;
	private final FileMetaFactory fileMetaFactory;
	private final IdGenerator idGenerator;
	private final DataService dataService;

	public FileStoreImpl(String storageDir, FileMetaFactory fileMetaFactory, IdGenerator idGenerator,
			DataService dataService)
	{
		this.storageDir = requireNonNull(storageDir);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.idGenerator = requireNonNull(idGenerator);
		this.dataService = requireNonNull(dataService);
	}

	public boolean createDirectory(String dirName) throws IOException
	{
		return new File(storageDir + separator + dirName).mkdir();
	}

	public void deleteDirectory(String dirName) throws IOException
	{
		FileUtils.deleteDirectory(getFile(dirName));
	}

	public File store(InputStream is, String fileName) throws IOException
	{
		File file = new File(storageDir + separator + fileName);
		FileOutputStream fos = new FileOutputStream(file);
		try
		{
			IOUtils.copy(is, fos);
		}
		finally
		{
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(is);
		}
		return file;
	}

	@Override
	public File getFile(FileMeta fileMeta)
	{
		return getFile(fileMeta.getId());
	}

	@Override
	public FileMeta createFileMeta(String filename)
	{
		String id = idGenerator.generateId(SHORT_RANDOM);
		FileMeta fileMeta = fileMetaFactory.create(id);
		fileMeta.setContentType(guessContentTypeFromName(filename));
		fileMeta.setFilename(filename);
		fileMeta.setUrl(format("{0}/{1}", "/files", id));
		dataService.add(FILE_META, fileMeta);
		return fileMeta;
	}

	@Override
	public void write(InputStream is, FileMeta fileMeta) throws IOException
	{
		File file = getFile(fileMeta);
		copyInputStreamToFile(is, file);
		fileMeta.setSize(file.length());
		dataService.update(FILE_META, fileMeta);
	}

	public File getFile(String fileName)
	{
		return new File(storageDir + separator + fileName);
	}

	public boolean delete(String fileName)
	{
		File file = new File(storageDir + separator + fileName);
		return file.delete();
	}

	public String getStorageDir()
	{
		return storageDir;
	}

	public void writeToFile(InputStream inputStream, String fileName) throws IOException
	{
		copyInputStreamToFile(inputStream, getFile(fileName));
	}

}
