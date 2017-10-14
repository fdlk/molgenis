package org.molgenis.data.rdf;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config
{
	static final String REPOSITORY_URL = "http://localhost:7200/repositories/molgenis";

	@Bean
	public Repository repository()
	{
		return new HTTPRepository(REPOSITORY_URL);
	}

	@Bean
	public TripleStore tripleStore()
	{
		return new TripleStore(repository());
	}
}
