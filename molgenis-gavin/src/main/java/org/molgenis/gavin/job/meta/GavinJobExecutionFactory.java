package org.molgenis.gavin.job.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.gavin.job.GavinJobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GavinJobExecutionFactory
    extends AbstractSystemEntityFactory<GavinJobExecution, GavinJobExecutionMetaData, String> {
  @Autowired
  GavinJobExecutionFactory(
      GavinJobExecutionMetaData gavinJobExecutionMetaData, EntityPopulator entityPopulator) {
    super(GavinJobExecution.class, gavinJobExecutionMetaData, entityPopulator);
  }
}
