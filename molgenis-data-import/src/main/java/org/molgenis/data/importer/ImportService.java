package org.molgenis.data.importer;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.springframework.core.Ordered;

public interface ImportService extends Ordered {
  EntityImportReport doImport(
      RepositoryCollection source, DatabaseAction databaseAction, String packageId);

  EntitiesValidationReport validateImport(File file, RepositoryCollection source);

  boolean canImport(File file, RepositoryCollection source);

  List<DatabaseAction> getSupportedDatabaseActions();

  boolean getMustChangeEntityName();

  Set<String> getSupportedFileExtensions();

  LinkedHashMap<String, Boolean> determineImportableEntities(
      MetaDataService metaDataService,
      RepositoryCollection repositoryCollection,
      String defaultPackage);
}
