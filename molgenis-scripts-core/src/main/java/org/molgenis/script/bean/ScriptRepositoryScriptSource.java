package org.molgenis.script.bean;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.io.IOException;
import java.util.Optional;

import org.molgenis.data.DataService;
import org.molgenis.script.Script;
import org.springframework.scripting.ScriptSource;

/**
 * Class that loads scripts from the Script Repository.
 */
public class ScriptRepositoryScriptSource implements ScriptSource
{
	private final DataService dataService;
	private final String name;
	private Optional<String> previousResult = Optional.empty();

	public ScriptRepositoryScriptSource(String name, DataService dataService)
	{
		this.dataService = dataService;
		this.name = name;
	}

	@Override
	public String getScriptAsString() throws IOException
	{
		return runAsSystem(() -> {
			Script script = dataService.findOne(Script.ENTITY_NAME, name, Script.class);
			if (script != null)
			{
				previousResult = Optional.of(script.getContent());
				return script.getContent();
			}
			return null;
		});
	}

	@Override
	public boolean isModified()
	{
		if (!previousResult.isPresent())
		{
			return runAsSystem(() -> {
				return dataService.findOne(Script.ENTITY_NAME, name) != null;
			});
		}
		try
		{
			return !previousResult.get().equals(getScriptAsString());
		}
		catch (IOException e)
		{
			return true;
		}
	}

	@Override
	public String suggestedClassName()
	{
		return name;
	}

}
