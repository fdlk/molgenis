package org.molgenis.data.importer.generic;

import org.molgenis.data.file.FileContainer;
import org.molgenis.data.meta.model.EntityType;

import java.util.List;

public interface GenericImporterService
{
	List<EntityType> importFile(FileContainer fileContainer);
}
