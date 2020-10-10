package org.molgenis.bootstrap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.bootstrap.populate.PermissionPopulator;
import org.molgenis.bootstrap.populate.RepositoryPopulator;
import org.molgenis.data.event.BootstrappingEventPublisher;
import org.molgenis.data.importer.ImportBootstrapper;
import org.molgenis.data.index.bootstrap.IndexBootstrapper;
import org.molgenis.data.migrate.bootstrap.MolgenisUpgradeBootstrapper;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistrar;
import org.molgenis.jobs.JobBootstrapper;
import org.molgenis.security.acl.DataSourceAclTablesPopulator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

class BootstrapperTest extends AbstractMockitoTest {
  @Mock private MolgenisUpgradeBootstrapper upgradeBootstrapper;
  @Mock private DataSourceAclTablesPopulator dataSourceAclTablesPopulator;
  @Mock private TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar;
  @Mock private RegistryBootstrapper registryBootstrapper;
  @Mock private SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
  @Mock private RepositoryPopulator repositoryPopulator;
  @Mock private PermissionPopulator systemPermissionPopulator;
  @Mock private JobBootstrapper jobBootstrapper;
  @Mock private ImportBootstrapper importBootstrapper;
  @Mock private IndexBootstrapper indexBootstrapper;
  @Mock private EntityTypeRegistryPopulator entityTypeRegistryPopulator;
  @Mock private BootstrappingEventPublisher bootstrappingEventPublisher;

  private Bootstrapper bootstrapper;

  @BeforeEach
  void setUpBeforeMethod() {
    bootstrapper =
        new Bootstrapper(
            upgradeBootstrapper,
            dataSourceAclTablesPopulator,
            transactionExceptionTranslatorRegistrar,
            registryBootstrapper,
            systemEntityTypeBootstrapper,
            repositoryPopulator,
            systemPermissionPopulator,
            jobBootstrapper,
            importBootstrapper,
            indexBootstrapper,
            entityTypeRegistryPopulator,
            bootstrappingEventPublisher);
  }

  @Test
  void testMolgenisBootstrapper() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Bootstrapper(
                null, null, null, null, null, null, null, null, null, null, null, null));
  }

  @Test
  void testOnApplicationEvent() {
    ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(event.getApplicationContext()).thenReturn(applicationContext);

    bootstrapper.bootstrap(event);

    verify(bootstrappingEventPublisher).publishBootstrappingStartedEvent();
    verify(upgradeBootstrapper).bootstrap();
    verify(dataSourceAclTablesPopulator).populate();
    verify(transactionExceptionTranslatorRegistrar).register(applicationContext);
    verify(registryBootstrapper).bootstrap(event);
    verify(systemEntityTypeBootstrapper).bootstrap(event);
    verify(repositoryPopulator).populate(event);
    verify(systemPermissionPopulator).populate(applicationContext);
    verify(jobBootstrapper).bootstrap();
    verify(importBootstrapper).bootstrap();
    verify(indexBootstrapper).bootstrap();
    verify(entityTypeRegistryPopulator).populate();
    verify(bootstrappingEventPublisher).publishBootstrappingFinishedEvent();
  }
}
