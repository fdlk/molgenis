package org.molgenis.file;

import org.molgenis.file.model.FileMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FileStore
{

	/**
	 * Get the File for a given {@link FileMeta}.
	 *
	 * @param fileMeta the {@link FileMeta} that the file belongs to
	 * @return File for the {@link FileMeta}
	 */
	File getFile(FileMeta fileMeta);

	/**
	 * Creates a new FileMeta for a given filename and adds it to the database.
	 *
	 * @param filename name of the file, including extension
	 * @return the created FileMeta
	 */
	FileMeta createFileMeta(String filename);

	/**
	 * Writes data to a file and updates the FileMeta.
	 *
	 * @param is       InputStream to read the data from.
	 * @param fileMeta {@link FileMeta} for the file
	 * @throws IOException if something goes wrong writing to the file
	 */
	void write(InputStream is, FileMeta fileMeta) throws IOException;
}
