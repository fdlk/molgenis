package org.molgenis.data.importer.table;

import static java.lang.String.format;

class InvalidTableCollectionException extends RuntimeException
{
	public InvalidTableCollectionException(String fileName)
	{
		super(format("File [%s] does not represent a table collection", fileName));
	}
}
