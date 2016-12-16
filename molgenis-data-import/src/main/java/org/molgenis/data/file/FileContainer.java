package org.molgenis.data.file;

import com.google.auto.value.AutoValue;
import org.molgenis.file.model.FileMeta;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;

@AutoValue
public abstract class FileContainer
{
	@NotNull
	public abstract FileMeta getFileMeta();

	@NotNull
	public abstract Path getFilePath();

	public static FileContainer create(FileMeta fileMeta, Path path)
	{
		return new AutoValue_FileContainer(fileMeta, path);
	}
}
