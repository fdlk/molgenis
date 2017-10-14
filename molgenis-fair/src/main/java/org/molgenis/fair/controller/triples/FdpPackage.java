package org.molgenis.fair.controller.triples;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class FdpPackage extends SystemPackage
{
	private static final String SIMPLE_NAME = "fdp";
	public static final String PACKAGE_FDP = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	public FdpPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super(PACKAGE_FDP, packageMetadata);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("FDP");
		setDescription("Package containing Fair Datapoint entities");
		setParent(rootSystemPackage);
	}
}
