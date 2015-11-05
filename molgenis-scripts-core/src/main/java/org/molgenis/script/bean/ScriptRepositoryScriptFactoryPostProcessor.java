package org.molgenis.script.bean;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.ScriptFactoryPostProcessor;
import org.springframework.scripting.support.StaticScriptSource;

public class ScriptRepositoryScriptFactoryPostProcessor extends ScriptFactoryPostProcessor
{

	public static final String REPOSITORY_SCRIPT_PREFIX = "repository:";

	private DataService dataService;

	@Required
	public void setDataSource(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	protected ScriptSource convertToScriptSource(String beanName, String scriptSourceLocator,
			ResourceLoader resourceLoader)
	{
		if (scriptSourceLocator.startsWith(INLINE_SCRIPT_PREFIX))
		{
			return new StaticScriptSource(scriptSourceLocator.substring(INLINE_SCRIPT_PREFIX.length()), beanName);
		}
		else if (scriptSourceLocator.startsWith(REPOSITORY_SCRIPT_PREFIX))
		{
			return new ScriptRepositoryScriptSource(scriptSourceLocator.substring(REPOSITORY_SCRIPT_PREFIX.length()),
					dataService);
		}
		else
		{
			return new ResourceScriptSource(resourceLoader.getResource(scriptSourceLocator));
		}
	}

}
