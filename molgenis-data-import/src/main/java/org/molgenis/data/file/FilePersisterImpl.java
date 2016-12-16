package org.molgenis.data.file;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;

@Component
public class FilePersisterImpl implements FilePersister
{
	private final FileStore fileStore;
	private final IdGenerator idGenerator;
	private final FileMetaFactory fileMetaFactory;
	private final DataService dataService;

	@Autowired
	public FilePersisterImpl(FileStore fileStore, IdGenerator idGenerator, FileMetaFactory fileMetaFactory,
			DataService dataService)
	{
		this.fileStore = requireNonNull(fileStore);
		this.idGenerator = requireNonNull(idGenerator);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public FileContainer persistFile(MultipartFile multipartFile)
	{
		String id = idGenerator.generateId();
		File file;
		try
		{
			file = fileStore.store(multipartFile.getInputStream(), id);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}

		FileMeta fileMeta = fileMetaFactory.create(id);
		fileMeta.setFilename(multipartFile.getOriginalFilename());
		fileMeta.setContentType(multipartFile.getContentType());
		fileMeta.setSize(multipartFile.getSize());
		fileMeta.setUrl(
				ServletUriComponentsBuilder.fromCurrentRequest().replacePath(FileDownloadController.URI + '/' + id)
						.replaceQuery(null).build().toUriString());
		dataService.add(FILE_META, fileMeta);
		return FileContainer.create(fileMeta, file.toPath());
	}
}
