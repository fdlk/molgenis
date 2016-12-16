package org.molgenis.data.importer.generic;

import org.molgenis.data.file.FileContainer;
import org.molgenis.data.file.FilePersister;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.importer.generic.GenericImporterController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class GenericImporterController extends MolgenisPluginController
{
	public static final String ID = "genericimporter";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final GenericImporterService genericImporterService;
	private final FilePersister filePersister;

	@Autowired
	public GenericImporterController(GenericImporterService genericImporterService, FilePersister filePersister)
	{
		super(URI);
		this.genericImporterService = requireNonNull(genericImporterService);
		this.filePersister = requireNonNull(filePersister);
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-genericimporter";
	}

	@RequestMapping(value = "/import")
	public String doImport(@RequestParam(value = "file", required = false) MultipartFile multipartFile, Model model)
			throws IOException
	{
		FileContainer fileContainer = filePersister.persistFile(multipartFile);
		List<EntityType> entityTypes = genericImporterService.importFile(fileContainer);
		model.addAttribute("entityTypes", entityTypes);
		return "view-genericimporter";
	}
}