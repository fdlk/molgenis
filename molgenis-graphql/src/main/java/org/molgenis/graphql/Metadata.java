package org.molgenis.graphql;

import com.bretpatterson.schemagen.graphql.annotations.GraphQLController;
import com.bretpatterson.schemagen.graphql.annotations.GraphQLParam;
import com.bretpatterson.schemagen.graphql.annotations.GraphQLQuery;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.graphql.model.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

@GraphQLController
@Service
public class Metadata
{
	private final MetaDataService metaDataService;

	@Autowired
	public Metadata(MetaDataService metaDataService){
		this.metaDataService = requireNonNull(metaDataService);
	}

	@GraphQLQuery(name = "attributes")
	public List<Attribute> getEntity(@GraphQLParam(name = "name") String name)
	{
		return StreamSupport.stream(metaDataService.getEntityType(name).getAttributes().spliterator(), false)
				.map(this::toAttribute).collect(Collectors.toList());
	}

	private Attribute toAttribute(org.molgenis.data.meta.model.Attribute attribute)
	{
		return Attribute.builder().setName(attribute.getName()).setDescription(attribute.getDescription())
				.setLabel(attribute.getLabel()).build();
	}
}
