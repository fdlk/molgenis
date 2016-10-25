package org.molgenis.data.discovery.meta.matching;

import org.molgenis.data.discovery.meta.BiobankUniversePackage;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ENUM;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.discovery.meta.BiobankUniversePackage.PACKAGE_UNIVERSE;
import static org.molgenis.data.discovery.meta.matching.AttributeMappingDecisionMetaData.DecisionOptions.getEnumValues;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

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

		private static final Map<String, DecisionOptions> strValMap;

		static
		{
			DecisionOptions[] decisionOptionValues = DecisionOptions.values();
			strValMap = newHashMapWithExpectedSize(decisionOptionValues.length);
			for (DecisionOptions decisionOption : decisionOptionValues)
			{
				strValMap.put(decisionOption.toString().toLowerCase(), decisionOption);
			}
		}

		public static List<String> getEnumValues()
		{
			return Stream.of(DecisionOptions.values()).map(DecisionOptions::toString).collect(toList());
		}

		public static DecisionOptions toEnum(String valueString)
		{
			String lowerCase = valueString.toLowerCase();
			return strValMap.containsKey(lowerCase) ? strValMap.get(lowerCase) : NO;
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
		addAttribute(DECISION).setDataType(ENUM).setEnumOptions(getEnumValues());
		addAttribute(COMMENT).setDataType(TEXT).setNillable(true);
		addAttribute(OWNER);
	}
}
