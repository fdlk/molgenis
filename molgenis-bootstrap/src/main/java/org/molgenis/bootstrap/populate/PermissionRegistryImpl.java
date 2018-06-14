package org.molgenis.bootstrap.populate;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.core.ui.admin.user.UserAccountController;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.util.Pair;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.security.account.AccountService.ROLE_USER;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.SidUtils.createRoleSid;

@Component
public class PermissionRegistryImpl implements PermissionRegistry
{
	private final DataService dataService;

	public PermissionRegistryImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> getPermissions()
	{
		ImmutableMultimap.Builder<ObjectIdentity, Pair<PermissionSet, Sid>> mapBuilder = new ImmutableMultimap.Builder<>();

		Sid userRoleSid = createRoleSid(ROLE_USER);

		ObjectIdentity pluginIdentity = new PluginIdentity(UserAccountController.ID);
		mapBuilder.putAll(pluginIdentity, new Pair<>(READ, userRoleSid));

		dataService.findAll(ENTITY_TYPE_META_DATA,
				Stream.of(ENTITY_TYPE_META_DATA, ATTRIBUTE_META_DATA, PACKAGE, TAG, LANGUAGE, L10N_STRING, FILE_META,
						DECORATOR_CONFIGURATION), EntityType.class).forEach(entityType ->
		{
			ObjectIdentity entityTypeIdentity = new EntityTypeIdentity(entityType);
			mapBuilder.putAll(entityTypeIdentity, new Pair<>(READ, userRoleSid));
		});

		return mapBuilder.build();
	}
}
