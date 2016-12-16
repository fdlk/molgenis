package org.molgenis.data.file;

import org.springframework.web.multipart.MultipartFile;

public interface FilePersister
{
	FileContainer persistFile(MultipartFile multipartFile);
}
