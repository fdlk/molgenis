package org.molgenis.graphql;

import com.bretpatterson.schemagen.graphql.annotations.GraphQLController;
import com.bretpatterson.schemagen.graphql.annotations.GraphQLParam;
import com.bretpatterson.schemagen.graphql.annotations.GraphQLQuery;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	@GraphQLQuery(name = "entityType")
	public EntityType getEntity(@GraphQLParam(name = "name") String name) {
		return metaDataService.getEntityType(name);
	}
}
