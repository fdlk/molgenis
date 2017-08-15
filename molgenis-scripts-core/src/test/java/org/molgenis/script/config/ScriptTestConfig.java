package org.molgenis.script.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.script.ScriptFactory;
import org.molgenis.script.ScriptMetaData;
import org.molgenis.script.ScriptPackage;
import org.molgenis.script.ScriptParameterFactory;
import org.molgenis.script.ScriptParameterMetaData;
import org.molgenis.script.ScriptTypeFactory;
import org.molgenis.script.ScriptTypeMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  ScriptMetaData.class,
  ScriptTypeMetaData.class,
  ScriptParameterMetaData.class,
  ScriptFactory.class,
  ScriptTypeFactory.class,
  ScriptParameterFactory.class,
  ScriptPackage.class
})
public class ScriptTestConfig {}
