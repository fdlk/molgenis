import java.io.IOException;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.RefreshableScriptTargetSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = TestGroovy.Config.class)
public class TestGroovy extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private Calculator calculator;

	@BeforeMethod
	public void beforeMethod()
	{
		Mockito.reset(dataService);
	}

	@Test
	public void testCalculator() throws InterruptedException
	{
		while(true){
			System.out.println(calculator.add(3, 4));
			Thread.sleep(1000);
		}
		
//		Assert.assertEquals(7, calculator.add(3, 4));
	}

	// @ImportResource(value =
	// { "classpath:calculator.xml" })
	@Configuration
	public static class Config
	{

		@Bean
		public DataService dataService()
		{
			return Mockito.mock(DataService.class);
		}

		@Bean
		public Calculator calculator(BeanFactory beanFactory) throws ScriptCompilationException, IOException
		{
			GroovyScriptFactory factory = new GroovyScriptFactory("classpath");
			factory.setBeanFactory(beanFactory);

			ResourceScriptSource script = new ResourceScriptSource(new ClassPathResource("Calculator.groovy"));

			RefreshableScriptTargetSource rsts = new RefreshableScriptTargetSource(beanFactory, "ignored-bean-name", factory, script, false) {

				@Override
				protected Object obtainFreshBean(BeanFactory beanFactory, String beanName) {
					
					/*
					 * we ask the factory to create a new script bean directly instead
					 * asking the beanFactory for simplicity. 
					 */
					try {
						return factory.getScriptedObject(script);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
			rsts.setRefreshCheckDelay(1000L);

			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setTargetSource(rsts);
			proxyFactory.setInterfaces(Calculator.class);

//			DelegatingIntroductionInterceptor introduction = new DelegatingIntroductionInterceptor(rsts);
//			introduction.suppressInterface(TargetSource.class);
//			proxyFactory.addAdvice(introduction);
			
			return (Calculator) proxyFactory.getProxy();
		}
	}
}
