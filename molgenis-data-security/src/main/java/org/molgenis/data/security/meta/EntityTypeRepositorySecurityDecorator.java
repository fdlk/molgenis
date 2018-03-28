package org.molgenis.data.security.meta;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.EntityIdentityUtils;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator.Action.CREATE;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSystem;

/**
 * Decorator for the entity type repository:
 * - filters requested entities based on the permissions of the current user
 * - validates permissions when adding, updating or deleting entity types
 * <p>
 */
public class EntityTypeRepositorySecurityDecorator extends AbstractRowLevelSecurityRepositoryDecorator<EntityType>
{
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final MutableAclService mutableAclService;
	private final MutableAclClassService mutableAclClassService;

	public EntityTypeRepositorySecurityDecorator(Repository<EntityType> delegateRepository,
			SystemEntityTypeRegistry systemEntityTypeRegistry, UserPermissionEvaluator userPermissionEvaluator,
			MutableAclService mutableAclService, MutableAclClassService mutableAclClassService)
	{
		super(delegateRepository, mutableAclService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
	}

	protected String toMessagePermission(Action action)
	{
		return getPermissionForOperation(action).getName();
	}

	@Override
	public boolean isOperationPermitted(EntityType entityType, Action action)
	{
		return isOperationPermitted(entityType.getId(), action);
	}

	@Override
	public boolean isOperationPermitted(Object id, Action action)
	{
		AbstractPermission permission = getPermissionForOperation(action);
		boolean hasPermission = userPermissionEvaluator.hasPermission(new EntityTypeIdentity(id.toString()),
				permission);
		if (hasPermission && !permission.equals(EntityTypePermission.COUNT))
		{
			boolean isSystem = systemEntityTypeRegistry.hasSystemEntityType(id.toString());
			if (isSystem && !currentUserIsSystem())
			{
				throw new MolgenisDataException(
						format("No [%s] permission on EntityType [%s]", toMessagePermission(action), id));
			}
		}
		return hasPermission;
	}

	private static EntityTypePermission getPermissionForOperation(Action action)
	{
		EntityTypePermission permission;
		switch (action)
		{
			case COUNT:
			case READ:
				permission = EntityTypePermission.COUNT;
				break;
			case UPDATE:
			case DELETE:
				permission = EntityTypePermission.WRITEMETA;
				break;
			case CREATE:
				throw new UnexpectedEnumException(CREATE);
			default:
				throw new IllegalArgumentException("Illegal entity type permission");
		}
		return permission;
	}

	@Override
	public void createAcl(EntityType entityType)
	{
		MutableAcl acl = mutableAclService.createAcl(new EntityTypeIdentity(entityType.getId()));
		Package pack = entityType.getPackage();
		verifyAddEntityTypeToPackageIsAllowed(pack);
		if (pack != null)
		{
			acl.setParent(mutableAclService.readAclById(new PackageIdentity(pack)));
			mutableAclService.updateAcl(acl);
		}
	}

	@Override
	public void deleteAcl(EntityType entityType)
	{
		mutableAclService.deleteAcl(new EntityTypeIdentity(entityType), true);
		mutableAclClassService.deleteAclClass(EntityIdentityUtils.toType(entityType));
	}

	@Override
	public void deleteAcl(Object entityTypeId)
	{
		mutableAclService.deleteAcl(new EntityTypeIdentity((String) entityTypeId), true);
		mutableAclClassService.deleteAclClass(EntityIdentityUtils.toType((String) entityTypeId));
	}

	@Override
	public void updateAcl(EntityType entityType)
	{
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new EntityTypeIdentity(entityType.getId()));
		Package newPackage = entityType.getPackage();

		if (newPackage == null)
		{
			if (acl.getParentAcl() != null)
			{
				// removed from package
				verifyAddEntityTypeToPackageIsAllowed(null);
				acl.setParent(null);
				mutableAclService.updateAcl(acl);
			}
		}
		else
		{
			PackageIdentity packageIdentity = new PackageIdentity(newPackage);
			if (acl.getParentAcl() == null || !acl.getParentAcl().getObjectIdentity().equals(packageIdentity))
			{
				// moved to new existing package
				verifyAddEntityTypeToPackageIsAllowed(newPackage);
				acl.setParent(mutableAclService.readAclById(packageIdentity));
				mutableAclService.updateAcl(acl);
			}
		}
	}

	private void verifyAddEntityTypeToPackageIsAllowed(Package pack)
	{
		if (pack == null)
		{
			if (!SecurityUtils.currentUserIsSuOrSystem())
			{
				throw new MolgenisDataAccessException(
						"Only superusers are allowed to create entity types without a package");
			}
		}
		else
		{
			if (!userPermissionEvaluator.hasPermission(new PackageIdentity(pack), EntityTypePermission.WRITEMETA))
			{
				throw new MolgenisDataAccessException(format("No [%s] permission on package [%s] with id [%s]",
						EntityTypePermission.WRITEMETA.getName(), pack.getLabel(), pack.getId()));
			}
		}
	}

}