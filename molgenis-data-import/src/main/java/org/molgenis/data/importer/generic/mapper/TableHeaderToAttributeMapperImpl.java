package org.molgenis.data.importer.generic.mapper;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;

@Component
public class TableHeaderToAttributeMapperImpl implements TableHeaderToAttributeMapper
{
	private final AttributeFactory attrFactory;

	@Autowired
	public TableHeaderToAttributeMapperImpl(AttributeFactory attrFactory)
	{
		this.attrFactory = requireNonNull(attrFactory);
	}

	@Override
	public MappedAttribute create(int index, String header)
	{
		String name = generateName(header);
		boolean isIdAttr = index == 0;
		boolean isLabelAttr = index == 0;
		Integer lookupAttrIdx = index == 0 ? 0 : null;
		AttributeType attrType = isIdAttr ? STRING : TEXT;

		Attribute attr = attrFactory.create().setName(name).setLabel(header).setDataType(attrType)
				.setIdAttribute(isIdAttr).setLabelAttribute(isLabelAttr).setLookupAttributeIndex(lookupAttrIdx)
				.setNillable(!isIdAttr).setUnique(isIdAttr).setSequenceNumber(index);

		return MappedAttribute.create(index, header, attr);
	}

	private String generateName(String header)
	{
		String attrName = header.replaceAll("[\\W+]", "");
		if (attrName.length() > 30)
		{
			attrName = attrName.substring(0, 30);
		}
		return attrName;
	}
}
