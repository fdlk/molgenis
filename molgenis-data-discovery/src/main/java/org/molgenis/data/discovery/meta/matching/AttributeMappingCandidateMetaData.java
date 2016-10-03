package org.molgenis.data.discovery.meta.matching;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.discovery.meta.BiobankUniversePackage.PACKAGE_UNIVERSE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.discovery.meta.BiobankUniversePackage;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData;
import org.molgenis.data.discovery.meta.biobank.BiobankUniverseMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class AttributeMappingCandidateMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "AttributeMappingCandidate";
	public static final String ATTRIBUTE_MAPPING_CANDIDATE = PACKAGE_UNIVERSE + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String BIOBANK_UNIVERSE = "biobankUniverse";
	public static final String TARGET = "target";
	public static final String SOURCE = "source";
	public static final String EXPLANATION = "explanation";
	public static final String DECISIONS = "decisions";

	private final BiobankUniversePackage biobankUniversePackage;
	private final BiobankUniverseMetaData biobankUniverseMetaData;
	private final BiobankSampleAttributeMetaData biobankSampleAttributeMetaData;
	private final MatchingExplanationMetaData matchingExplanationMetaData;
	private final AttributeMappingDecisionMetaData attributeMappingDecisionMetaData;

	public AttributeMappingCandidateMetaData(BiobankUniversePackage biobankUniversePackage,
			BiobankUniverseMetaData biobankUniverseMetaData,
			BiobankSampleAttributeMetaData biobankSampleAttributeMetaData,
			MatchingExplanationMetaData matchingExplanationMetaData,
			AttributeMappingDecisionMetaData attributeMappingDecisionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_UNIVERSE);

		this.biobankUniversePackage = requireNonNull(biobankUniversePackage);
		this.biobankUniverseMetaData = requireNonNull(biobankUniverseMetaData);
		this.biobankSampleAttributeMetaData = requireNonNull(biobankSampleAttributeMetaData);
		this.matchingExplanationMetaData = requireNonNull(matchingExplanationMetaData);
		this.attributeMappingDecisionMetaData = requireNonNull(attributeMappingDecisionMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("Attribute mapping candidate");
		setPackage(biobankUniversePackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(BIOBANK_UNIVERSE).setDataType(XREF).setRefEntity(biobankUniverseMetaData);
		addAttribute(TARGET).setDataType(XREF).setRefEntity(biobankSampleAttributeMetaData);
		addAttribute(SOURCE).setDataType(XREF).setRefEntity(biobankSampleAttributeMetaData);
		addAttribute(EXPLANATION).setDataType(XREF).setRefEntity(matchingExplanationMetaData);
		addAttribute(DECISIONS).setDataType(MREF).setRefEntity(attributeMappingDecisionMetaData).setNillable(true);
	}
}
