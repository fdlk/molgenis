package org.molgenis.data.importer.table.zip;

import org.molgenis.data.importer.table.Table;

import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipTableIterator implements Iterator<Table>
{
	private final ZipInputStream zipInputStream;

	ZipTableIterator(ZipInputStream zipInputStream)
	{
		this.zipInputStream = zipInputStream;
	}

	@Override
	public boolean hasNext()
	{

		try
		{
			for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null; )
			{

			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Table next()
	{
		return null;
	}
}
