package org.molgenis.data.postgresql;

import javax.sql.DataSource;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.util.AttributeCopier;
import org.molgenis.data.meta.util.EntityTypeCopier;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgreSqlConfiguration {
  @Autowired private PostgreSqlEntityFactory postgreSqlEntityFactory;

  @Autowired private DataSource dataSource;

  @Autowired private DataService dataService;

  @Autowired private PostgreSqlExceptionTranslator postgreSqlExceptionTranslator;

  @Autowired private EntityTypeRegistry entityTypeRegistry;

  @Autowired private EntityTypeCopier entityTypeCopier;

  @Autowired private AttributeCopier attributeCopier;

  @Bean
  public JdbcTemplate jdbcTemplate() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.setExceptionTranslator(postgreSqlExceptionTranslator);
    return jdbcTemplate;
  }

  @Bean
  public RepositoryCollection postgreSqlRepositoryCollection() {
    return new PostgreSqlRepositoryCollectionDecorator(
        new PostgreSqlRepositoryCollection(
            postgreSqlEntityFactory, dataSource, jdbcTemplate(), dataService),
        entityTypeRegistry,
        entityTypeCopier,
        attributeCopier);
  }
}
