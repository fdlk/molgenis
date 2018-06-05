package org.molgenis.core.ui.data.importer.wizard;

import org.molgenis.core.ui.wizard.AbstractWizardPage;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.*;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ValidationResultWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ValidationResultWizardPage.class);

	private final transient ExecutorService asyncImportJobs = Executors.newCachedThreadPool();

	@Autowired
	private transient ImportServiceFactory importServiceFactory;

	@Autowired
	private transient FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	private transient ImportRunService importRunService;

	@Autowired
	transient UserAccountService userAccountService;

	@Autowired
	transient UserService userService;

	@Override
	public String getTitle()
	{
		return "Validation";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		ImportWizardUtil.validateImportWizard(wizard);
		ImportWizard importWizard = (ImportWizard) wizard;
		String entityImportOption = importWizard.getEntityImportOption();

		if (entityImportOption != null)
		{
			try
			{
				// convert input to database action
				DatabaseAction entityDbAction = ImportWizardUtil.toDatabaseAction(entityImportOption);
				if (entityDbAction == null) throw new IOException("unknown database action: " + entityImportOption);

				RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(
						importWizard.getFile());
				ImportService importService = importServiceFactory.getImportService(importWizard.getFile(),
						repositoryCollection);

				synchronized (this)
				{
					ImportRun importRun = importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false);
					((ImportWizard) wizard).setImportRunId(importRun.getId());

					asyncImportJobs.execute(
							new ImportJob(importService, SecurityContextHolder.getContext(), repositoryCollection,
									entityDbAction, importRun.getId(), importRunService, request.getSession(),
									importWizard.getSelectedPackage()));
				}

			}
			catch (RuntimeException | IOException e)
			{
				ImportWizardUtil.handleException(e, importWizard, result, LOG, entityImportOption);
			}

		}

		return null;
	}
}
