package org.molgenis.r;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.molgenis.script.ScriptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RScriptRunner implements ScriptRunner {
  private static final String NAME = "R";

  private final RScriptExecutor scriptExecutor;

  @Autowired
  public RScriptRunner(RScriptExecutor scriptExecutor) {
    this.scriptExecutor = requireNonNull(scriptExecutor);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String runScript(Script script, Map<String, Object> parameters) {
    String scriptWithParameters = ScriptUtils.generateScript(script, parameters);
    String outputFile = getOutputFile(parameters);
    return scriptExecutor.executeScript(scriptWithParameters, outputFile);
  }

  private String getOutputFile(Map<String, Object> parameters) {
    Object outputFile = parameters.get("outputFile");
    if (outputFile == null) {
      return null;
    }
    if (!(outputFile instanceof String)) {
      throw new RuntimeException(
          format(
              "Parameter outputFile is of type '%s' instead of '%s'",
              outputFile.getClass().getSimpleName(), String.class.getSimpleName()));
    }
    return (String) outputFile;
  }
}
