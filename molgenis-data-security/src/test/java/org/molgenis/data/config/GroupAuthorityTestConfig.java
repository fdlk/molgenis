package org.molgenis.data.config;

import org.molgenis.auth.AuthorityMetaData;
import org.molgenis.auth.GroupAuthorityFactory;
import org.molgenis.auth.GroupAuthorityMetaData;
import org.molgenis.auth.GroupMetaData;
import org.molgenis.auth.SecurityPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  GroupAuthorityMetaData.class,
  GroupAuthorityFactory.class,
  GroupMetaData.class,
  AuthorityMetaData.class,
  SecurityPackage.class
})
public class GroupAuthorityTestConfig {}
