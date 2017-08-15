package org.molgenis.data.mapper.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.UserTestConfig;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.mapper.meta.MapperPackage;
import org.molgenis.data.mapper.meta.MappingProjectMetaData;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  MappingProjectMetaData.class,
  MappingTargetMetaData.class,
  EntityMappingMetaData.class,
  AttributeMappingMetaData.class,
  UserTestConfig.class,
  MapperPackage.class
})
public class MapperTestConfig {}
