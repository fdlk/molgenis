package org.molgenis.data.importer.table;

import java.nio.file.Path;

import static java.lang.String.format;

class InvalidTableCollectionException extends RuntimeException
{
	public InvalidTableCollectionException(Path path)
	{
		super(format("File [%s] does not represent a table collection", path.getFileName().toString()));
	}
}
