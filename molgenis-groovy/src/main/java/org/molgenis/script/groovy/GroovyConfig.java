package org.molgenis.script.groovy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scripting.groovy.GroovyScriptFactory;

@Configuration
public class GroovyConfig
{
	@Bean
	GroovyScriptFactory bean(){
		return new GroovyScriptFactory("repository:TestBean");
	}
}
