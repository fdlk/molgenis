package org.molgenis.bootstrap;

import static java.util.Objects.requireNonNull;

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
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Application bootstrapper */
@Component
class Bootstrapper {
  private static final Logger LOG = LoggerFactory.getLogger(Bootstrapper.class);

  private final MolgenisUpgradeBootstrapper upgradeBootstrapper;
  private final DataSourceAclTablesPopulator dataSourceAclTablesPopulator;
  private final TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar;
  private final RegistryBootstrapper registryBootstrapper;
  private final SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
  private final RepositoryPopulator repositoryPopulator;
  private final PermissionPopulator systemPermissionPopulator;
  private final JobBootstrapper jobBootstrapper;
  private final ImportBootstrapper importBootstrapper;
  private final IndexBootstrapper indexBootstrapper;
  private final EntityTypeRegistryPopulator entityTypeRegistryPopulator;
  private final BootstrappingEventPublisher bootstrappingEventPublisher;

  Bootstrapper(
      MolgenisUpgradeBootstrapper upgradeBootstrapper,
      DataSourceAclTablesPopulator dataSourceAclTablesPopulator,
      TransactionExceptionTranslatorRegistrar transactionExceptionTranslatorRegistrar,
      RegistryBootstrapper registryBootstrapper,
      SystemEntityTypeBootstrapper systemEntityTypeBootstrapper,
      RepositoryPopulator repositoryPopulator,
      PermissionPopulator systemPermissionPopulator,
      JobBootstrapper jobBootstrapper,
      ImportBootstrapper importBootstrapper,
      IndexBootstrapper indexBootstrapper,
      EntityTypeRegistryPopulator entityTypeRegistryPopulator,
      BootstrappingEventPublisher bootstrappingEventPublisher) {
    this.upgradeBootstrapper = requireNonNull(upgradeBootstrapper);
    this.dataSourceAclTablesPopulator = requireNonNull(dataSourceAclTablesPopulator);
    this.transactionExceptionTranslatorRegistrar = transactionExceptionTranslatorRegistrar;
    this.registryBootstrapper = requireNonNull(registryBootstrapper);
    this.systemEntityTypeBootstrapper = requireNonNull(systemEntityTypeBootstrapper);
    this.repositoryPopulator = requireNonNull(repositoryPopulator);
    this.systemPermissionPopulator = requireNonNull(systemPermissionPopulator);
    this.jobBootstrapper = requireNonNull(jobBootstrapper);
    this.importBootstrapper = requireNonNull(importBootstrapper);
    this.indexBootstrapper = requireNonNull(indexBootstrapper);
    this.entityTypeRegistryPopulator = requireNonNull(entityTypeRegistryPopulator);
    this.bootstrappingEventPublisher = requireNonNull(bootstrappingEventPublisher);
  }

  @Transactional
  @RunAsSystem
  public void bootstrap(ContextRefreshedEvent event) {
    LOG.info("Bootstrapping application ...");
    bootstrappingEventPublisher.publishBootstrappingStartedEvent();

    LOG.trace("Updating MOLGENIS ...");
    upgradeBootstrapper.bootstrap();
    LOG.debug("Updated MOLGENIS");

    LOG.trace("Populating data source with ACL tables ...");
    dataSourceAclTablesPopulator.populate();
    LOG.debug("Populated data source with ACL tables");

    LOG.trace("Bootstrapping transaction exception translators ...");
    transactionExceptionTranslatorRegistrar.register(event.getApplicationContext());
    LOG.debug("Bootstrapped transaction exception translators");

    LOG.trace("Bootstrapping registries ...");
    registryBootstrapper.bootstrap(event);
    LOG.debug("Bootstrapped registries");

    LOG.trace("Bootstrapping system entity meta data ...");
    systemEntityTypeBootstrapper.bootstrap(event);
    LOG.debug("Bootstrapped system entity meta data");

    LOG.trace("Populating repositories ...");
    repositoryPopulator.populate(event);
    LOG.debug("Populated repositories");

    LOG.trace("Populating permissions ...");
    systemPermissionPopulator.populate(event.getApplicationContext());
    LOG.debug("Populated permissions");

    LOG.trace("Bootstrapping jobs ...");
    jobBootstrapper.bootstrap();
    LOG.debug("Bootstrapped jobs");

    LOG.trace("Bootstrapping import ...");
    importBootstrapper.bootstrap();
    LOG.debug("Bootstrapped import");

    LOG.trace("Bootstrapping index ...");
    indexBootstrapper.bootstrap();
    LOG.debug("Bootstrapped index");

    LOG.trace("Populating entity type registry ...");
    entityTypeRegistryPopulator.populate();
    LOG.debug("Populated entity type registry");

    bootstrappingEventPublisher.publishBootstrappingFinishedEvent();
    LOG.info("Bootstrapping application completed");
  }
}
