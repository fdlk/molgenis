package org.molgenis.data.importer.generic;

import org.molgenis.data.file.FileContainer;
import org.molgenis.data.file.FilePersister;
import org.molgenis.data.importer.generic.job.ImportExecutorService;
import org.molgenis.data.importer.generic.job.model.ImportJobExecution;
import org.molgenis.data.importer.generic.job.model.ImportJobExecutionMetadata;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.importer.generic.ImportController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class ImportController extends MolgenisPluginController
{
	public static final String ID = "importer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final FilePersister filePersister;
	private final ImportExecutorService importExecutorService;

	@Autowired
	public ImportController(FilePersister filePersister, ImportExecutorService importExecutorService)
	{
		super(URI);
		this.filePersister = requireNonNull(filePersister);
		this.importExecutorService = requireNonNull(importExecutorService);
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-importer";
	}

	@RequestMapping(value = "/import")
	public String doImport(@RequestParam(value = "file", required = false) MultipartFile multipartFile, Model model)
			throws IOException, ExecutionException, InterruptedException
	{
		FileContainer fileContainer = filePersister.persistFile(multipartFile);
		ImportJobExecution importJobExecution = importExecutorService.executeImport(fileContainer.getFileMeta());
		model.addAttribute("importJobHref",
				"/api/v2/" + ImportJobExecutionMetadata.IMPORT_JOB_EXECUTION + "/" + importJobExecution
						.getIdentifier());

		return "view-importer";
	}
}