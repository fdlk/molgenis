package org.molgenis.bootstrap.populate;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.Pair;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.testng.Assert.assertEquals;

public class PermissionRegistryImplTest extends AbstractMockitoTest
{
	@Mock(answer = RETURNS_DEEP_STUBS)
	private DataService dataService;

	@Mock
	private EntityType entityTypeEntityType;

	@Mock
	private EntityType attributeEntityType;

	@Captor
	private ArgumentCaptor<Stream<Object>> entityTypeIdCaptor;

	private PermissionRegistryImpl permissionRegistryImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		permissionRegistryImpl = new PermissionRegistryImpl(dataService);
	}

	@Test
	public void testGetPermissions()
	{
		when(entityTypeEntityType.getId()).thenReturn(ENTITY_TYPE_META_DATA);
		when(attributeEntityType.getId()).thenReturn(ATTRIBUTE_META_DATA);

		doReturn(Stream.of(entityTypeEntityType, attributeEntityType)).when(dataService)
																	  .findAll(eq(ENTITY_TYPE_META_DATA),
																			  entityTypeIdCaptor.capture(),
																			  eq(EntityType.class));

		GrantedAuthoritySid userSid = new GrantedAuthoritySid("ROLE_USER");
		Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> expectedPermissions = ImmutableListMultimap.of(
				new PluginIdentity("useraccount"), new Pair<>(READ, userSid),
				new EntityTypeIdentity(ENTITY_TYPE_META_DATA), new Pair<>(READ, userSid),
				new EntityTypeIdentity(ATTRIBUTE_META_DATA), new Pair<>(READ, userSid));
		assertEquals(permissionRegistryImpl.getPermissions(), expectedPermissions);

		assertEquals(entityTypeIdCaptor.getValue().collect(Collectors.toSet()),
				ImmutableSet.of(ENTITY_TYPE_META_DATA, ATTRIBUTE_META_DATA, PACKAGE, TAG, LANGUAGE, L10N_STRING,
						FILE_META, DECORATOR_CONFIGURATION));
	}
}