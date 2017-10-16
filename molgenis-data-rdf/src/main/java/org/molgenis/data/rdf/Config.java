package org.molgenis.data.rdf;

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Config
{
	static final String REPOSITORY_URL = "http://localhost:7200/";

	@Bean
	public RepositoryManager repositoryManager()
	{
		return RemoteRepositoryManager.getInstance(REPOSITORY_URL);
	}

	@Bean
	public TripleStore tripleStore() throws IOException
	{
		return new TripleStore(repositoryManager());
	}
}
