package org.molgenis.data.discovery.meta.matching;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ENUM;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.discovery.meta.BiobankUniversePackage.PACKAGE_UNIVERSE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import java.util.stream.Stream;

import org.molgenis.data.discovery.meta.BiobankUniversePackage;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AttributeMappingDecisionMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "AttributeMappingDecision";
	public static final String ATTRIBUTE_MAPPING_DECISION = PACKAGE_UNIVERSE + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String DECISION = "decision";
	public static final String COMMENT = "comment";
	public static final String OWNER = "owner";

	public static enum DecisionOptions
	{
		YES("Yes"), NO("No"), UNDECIDED("Undecided");

		private String label;

		DecisionOptions(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	private final BiobankUniversePackage biobankUniversePackage;

	@Autowired
	public AttributeMappingDecisionMetaData(BiobankUniversePackage biobankUniversePackage)
	{
		super(SIMPLE_NAME, PACKAGE_UNIVERSE);
		this.biobankUniversePackage = requireNonNull(biobankUniversePackage);
	}

	@Override
	protected void init()
	{
		setLabel("Attribute mapping decision");
		setPackage(biobankUniversePackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(DECISION).setDataType(ENUM)
				.setEnumOptions(Stream.of(DecisionOptions.values()).map(DecisionOptions::toString).collect(toList()));
		addAttribute(COMMENT).setDataType(TEXT).setNillable(true);
		addAttribute(OWNER);
	}
}
