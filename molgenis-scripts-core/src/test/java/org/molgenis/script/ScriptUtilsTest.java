package org.molgenis.script;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import org.testng.annotations.Test;

public class ScriptUtilsTest {
  @Test
  public void testGenerateScript() throws Exception {
    Script script = mock(Script.class);
    when(script.getContent()).thenReturn("Hey ${name}");
    Map<String, Object> parameterValues = Collections.singletonMap("name", "Piet");
    String renderedScript = ScriptUtils.generateScript(script, parameterValues);
    assertEquals(renderedScript, "Hey Piet");
  }
}
