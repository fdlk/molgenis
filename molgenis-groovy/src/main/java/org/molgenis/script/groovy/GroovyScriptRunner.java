package org.molgenis.script.groovy;

import java.util.Map;

import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.springframework.stereotype.Service;

/**
 * Runs a Groovy script with the given inputs and returns one output
 */
@Service
public class GroovyScriptRunner implements ScriptRunner
{
	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
//		Object scriptResult = jsScriptExecutor.executeScript(jsScript);
		return script.toString();
	}
}
