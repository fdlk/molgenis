package org.molgenis.file;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.file.model.FileMetaFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class FileStoreTest
{
	@Mock
	private FileMetaFactory fileMetaFactory;
	@Mock
	private IdGenerator idGenerator;
	@Mock
	private DataService dataService;
	private FileStoreImpl fileStore;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		fileStore = new FileStoreImpl(System.getProperty("java.io.tmpdir"), fileMetaFactory, idGenerator, dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void FileStore()
	{
		new FileStoreImpl(null, null, null, null);
	}

	@Test
	public void createDirectory() throws IOException
	{
		Assert.assertTrue(fileStore.createDirectory("testDir"));
		Assert.assertTrue(fileStore.getFile("testDir").isDirectory());
		fileStore.delete("testDir");
	}

	@Test
	public void store() throws IOException
	{
		File file = fileStore.store(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), "bytes.bin");
		Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] { 1, 2, 3 });
	}

	@Test
	public void getFile() throws IOException
	{
		String fileName = "bytes.bin";
		File file = fileStore.store(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), fileName);

		Assert.assertEquals(fileStore.getFile(fileName).getAbsolutePath(), file.getAbsolutePath());
	}
}
