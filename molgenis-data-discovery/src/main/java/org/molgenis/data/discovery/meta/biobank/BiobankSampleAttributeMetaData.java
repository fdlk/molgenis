package org.molgenis.data.discovery.meta.biobank;

import org.molgenis.data.discovery.meta.BiobankUniversePackage;
import org.molgenis.data.discovery.meta.matching.TagGroupMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.discovery.meta.BiobankUniversePackage.PACKAGE_UNIVERSE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class BiobankSampleAttributeMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "BiobankSampleAttribute";
	public static final String BIOBANK_SAMPLE_ATTRIBUTE = PACKAGE_UNIVERSE + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String COLLECTION = "collection";
	public static final String TAG_GROUPS = "tagGroups";

	private final BiobankUniversePackage biobankUniversePackage;
	private final BiobankSampleCollectionMetaData biobankSampleCollectionMetaData;
	private final TagGroupMetaData tagGroupMetaData;

	@Autowired
	public BiobankSampleAttributeMetaData(BiobankUniversePackage biobankUniversePackage,
			BiobankSampleCollectionMetaData biobankSampleCollectionMetaData, TagGroupMetaData tagGroupMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_UNIVERSE);
		this.biobankUniversePackage = requireNonNull(biobankUniversePackage);
		this.biobankSampleCollectionMetaData = requireNonNull(biobankSampleCollectionMetaData);
		this.tagGroupMetaData = requireNonNull(tagGroupMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("Biobank sample attribute");
		setPackage(biobankUniversePackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(NAME, ROLE_LABEL);
		addAttribute(LABEL);
		addAttribute(DESCRIPTION).setDataType(TEXT).setNillable(true);
		addAttribute(COLLECTION).setDataType(XREF).setRefEntity(biobankSampleCollectionMetaData);
		addAttribute(TAG_GROUPS).setDataType(MREF).setRefEntity(tagGroupMetaData).setNillable(true);

	}
}
