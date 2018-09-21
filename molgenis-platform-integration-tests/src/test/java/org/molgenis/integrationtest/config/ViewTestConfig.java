package org.molgenis.integrationtest.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.core.ui.menu.MenuReaderServiceImpl;
import org.molgenis.core.ui.style.MolgenisStyleException;
import org.molgenis.core.ui.style.StyleService;
import org.molgenis.core.ui.style.StyleSheetFactory;
import org.molgenis.core.ui.style.StyleSheetMetadata;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JobsController.class, StyleSheetFactory.class, StyleSheetMetadata.class})
public class ViewTestConfig {
  @Autowired private AppSettings appSettings;

  @Bean
  public MenuReaderService menuReaderService() {
    return new MenuReaderServiceImpl(appSettings);
  }

  @Bean
  public StyleService styleService() {
    return mock(StyleService.class);
  }

  @Bean
  public ThemeFingerprintRegistry themeFingerprintRegistry()
      throws IOException, MolgenisStyleException {
    ThemeFingerprintRegistry themeFingerprintRegistry = mock(ThemeFingerprintRegistry.class);
    when(themeFingerprintRegistry.getFingerprint(anyString())).thenReturn("");
    return themeFingerprintRegistry;
  }
}
