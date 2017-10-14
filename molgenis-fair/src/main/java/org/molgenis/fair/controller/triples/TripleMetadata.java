package org.molgenis.fair.controller.triples;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.fair.controller.triples.FdpPackage.PACKAGE_FDP;

@Component
public class TripleMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Triples";
	public static final String TRIPLE = PACKAGE_FDP + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String SUBJECT = "subject";
	public static final String PREDICATE = "relation";
	public static final String OBJECT = "object";

	private final FdpPackage fdpPackage;

	public TripleMetadata(FdpPackage fdpPackage)
	{
		super(SIMPLE_NAME, PACKAGE_FDP);
		this.fdpPackage = requireNonNull(fdpPackage);
	}

	@Override
	public void init()
	{
		setLabel("Triples");
		setPackage(fdpPackage);
		addAttribute(ID, ROLE_ID, ROLE_LABEL).setAuto(true).setNillable(false);
		addAttribute(SUBJECT, ROLE_LOOKUP).setLabel("Subject").setNillable(false);
		addAttribute(PREDICATE, ROLE_LOOKUP).setLabel("Relation").setNillable(false);
		addAttribute(OBJECT, ROLE_LOOKUP).setDataType(TEXT).setLabel("Object").setNillable(false);
	}
}
