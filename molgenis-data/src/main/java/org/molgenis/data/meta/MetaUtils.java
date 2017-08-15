package org.molgenis.data.meta;

import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.BACKEND;
import static org.molgenis.data.meta.model.EntityTypeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.EntityTypeMetadata.EXTENDS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ID;
import static org.molgenis.data.meta.model.EntityTypeMetadata.IS_ABSTRACT;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;
import static org.molgenis.data.meta.model.EntityTypeMetadata.PACKAGE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.TAGS;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Package;

public class MetaUtils {
  public static Fetch getEntityTypeFetch() {
    // TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
    return new Fetch()
        .field(ID)
        .field(PACKAGE)
        .field(LABEL)
        .field(DESCRIPTION)
        .field(ATTRIBUTES)
        .field(IS_ABSTRACT)
        .field(EXTENDS)
        .field(TAGS)
        .field(BACKEND);
  }

  /**
   * Returns whether the given package is a system package, i.e. it is the root system package or a
   * descendent of the root system package.
   *
   * @param package_ package
   * @return whether package is a system package
   */
  public static boolean isSystemPackage(Package package_) {
    return package_.getId().equals(PACKAGE_SYSTEM)
        || (package_.getRootPackage() != null
            && package_.getRootPackage().getId().equals(PACKAGE_SYSTEM));
  }

  public static String getFullyQualyfiedName(Package package_) {
    String packageId = package_.getId();
    Package parentPackage = package_.getParent();
    return parentPackage == null
        ? packageId
        : getFullyQualyfiedName(parentPackage) + PACKAGE_SEPARATOR + packageId;
  }
}
