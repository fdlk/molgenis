package org.molgenis.data.platform.decorators;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.owned.OwnedEntityType.OWNED;

import org.molgenis.data.CascadeDeleteRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityReferenceResolverDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.RepositorySecurityDecorator;
import org.molgenis.data.SystemRepositoryDecoratorRegistry;
import org.molgenis.data.aggregation.AggregateAnonymizer;
import org.molgenis.data.aggregation.AggregateAnonymizerRepositoryDecorator;
import org.molgenis.data.cache.l1.L1Cache;
import org.molgenis.data.cache.l1.L1CacheRepositoryDecorator;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.cache.l2.L2CacheRepositoryDecorator;
import org.molgenis.data.cache.l3.L3Cache;
import org.molgenis.data.cache.l3.L3CacheRepositoryDecorator;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryDecorator;
import org.molgenis.data.index.IndexedRepositoryDecorator;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.listeners.EntityListenerRepositoryDecorator;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionalRepositoryDecorator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.data.validation.QueryValidationRepositoryDecorator;
import org.molgenis.data.validation.QueryValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class MolgenisRepositoryDecoratorFactory implements RepositoryDecoratorFactory {
  private final EntityManager entityManager;
  private final EntityAttributesValidator entityAttributesValidator;
  private final AggregateAnonymizer aggregateAnonymizer;
  private final AppSettings appSettings;
  private final DataService dataService;
  private final ExpressionValidator expressionValidator;
  private final SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry;
  private final IndexActionRegisterService indexActionRegisterService;
  private final SearchService searchService;
  private final L1Cache l1Cache;
  private final EntityListenersService entityListenersService;
  private final L2Cache l2Cache;
  private final TransactionInformation transactionInformation;
  private final L3Cache l3Cache;
  private final PlatformTransactionManager transactionManager;
  private final QueryValidator queryValidator;

  @Autowired
  public MolgenisRepositoryDecoratorFactory(
      EntityManager entityManager,
      EntityAttributesValidator entityAttributesValidator,
      AggregateAnonymizer aggregateAnonymizer,
      AppSettings appSettings,
      DataService dataService,
      ExpressionValidator expressionValidator,
      SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry,
      IndexActionRegisterService indexActionRegisterService,
      SearchService searchService,
      L1Cache l1Cache,
      L2Cache l2Cache,
      TransactionInformation transactionInformation,
      EntityListenersService entityListenersService,
      L3Cache l3Cache,
      PlatformTransactionManager transactionManager,
      QueryValidator queryValidator) {

    this.entityManager = requireNonNull(entityManager);
    this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
    this.aggregateAnonymizer = requireNonNull(aggregateAnonymizer);
    this.appSettings = requireNonNull(appSettings);
    this.dataService = requireNonNull(dataService);
    this.expressionValidator = requireNonNull(expressionValidator);
    this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
    this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
    this.searchService = requireNonNull(searchService);
    this.l1Cache = requireNonNull(l1Cache);
    this.entityListenersService = requireNonNull(entityListenersService);
    this.l2Cache = requireNonNull(l2Cache);
    this.transactionInformation = requireNonNull(transactionInformation);
    this.l3Cache = requireNonNull(l3Cache);
    this.transactionManager = requireNonNull(transactionManager);
    this.queryValidator = requireNonNull(queryValidator);
  }

  @Override
  public Repository<Entity> createDecoratedRepository(Repository<Entity> repository) {
    Repository<Entity> decoratedRepository = repository;

    // 14. Query the L2 cache before querying the database
    decoratedRepository =
        new L2CacheRepositoryDecorator(decoratedRepository, l2Cache, transactionInformation);

    // 13. Query the L1 cache before querying the database
    decoratedRepository = new L1CacheRepositoryDecorator(decoratedRepository, l1Cache);

    // 12. Route specific queries to the index
    decoratedRepository = new IndexedRepositoryDecorator(decoratedRepository, searchService);

    // 11. Query the L3 cache before querying the index
    decoratedRepository =
        new L3CacheRepositoryDecorator(decoratedRepository, l3Cache, transactionInformation);

    // 10. Register the cud action needed to index indexed repositories
    decoratedRepository =
        new IndexActionRepositoryDecorator(decoratedRepository, indexActionRegisterService);

    // 9. Custom decorators
    decoratedRepository = repositoryDecoratorRegistry.decorate(decoratedRepository);

    // 8. Perform cascading deletes
    decoratedRepository = new CascadeDeleteRepositoryDecorator(decoratedRepository, dataService);

    // 7. Owned decorator
    if (EntityUtils.doesExtend(decoratedRepository.getEntityType(), OWNED)) {
      decoratedRepository = new OwnedEntityRepositoryDecorator(decoratedRepository);
    }

    // 6. Entity reference resolver decorator
    decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

    // 5. Entity listener
    decoratedRepository =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);

    // 4. validation decorator
    decoratedRepository =
        new RepositoryValidationDecorator(
            dataService, decoratedRepository, entityAttributesValidator, expressionValidator);

    // 3. aggregate anonymization decorator
    decoratedRepository =
        new AggregateAnonymizerRepositoryDecorator<>(
            decoratedRepository, aggregateAnonymizer, appSettings);

    // 2. security decorator
    decoratedRepository = new RepositorySecurityDecorator(decoratedRepository);

    // 1. transaction decorator
    decoratedRepository =
        new TransactionalRepositoryDecorator<>(decoratedRepository, transactionManager);

    // 0. query validation decorator
    decoratedRepository =
        new QueryValidationRepositoryDecorator<>(decoratedRepository, queryValidator);

    return decoratedRepository;
  }
}
