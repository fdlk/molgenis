package org.molgenis.ui.jobs;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@EnableBatchProcessing
public class JobsConfig
{
	@Bean
	public ItemReader<String> reader()
	{
		System.out.println("create reader()");
		return new IteratorItemReader<>(Arrays.asList("spinner1", "spinner2", "spinner3"));
	}

	@Bean
	public ItemProcessor<String, String> processor()
	{
		return new ItemProcessor<String, String>()
		{

			@Override
			public String process(String item) throws Exception
			{
				System.out.println("process" + item);
				Thread.sleep(3000);
				return item;
			}
		};
	}

	@Bean
	public ItemWriter<String> writer()
	{
		return new ItemWriter<String>()
		{
			@Override
			public void write(List<? extends String> items) throws Exception
			{
				System.out.println("write()" + items);
			}
		};
	}

	@Bean
	public Job sleepJob(JobBuilderFactory jobs, Step s1)
	{
		return jobs.get("sleepJob").incrementer(new RunIdIncrementer()).flow(s1).end().build();
	}

	@Bean
	public StepProgressMonitor<String> monitor()
	{
		return new StepProgressMonitor<String>();
	}

	@Bean
	public Step dummy(StepBuilderFactory stepBuilderFactory, ItemReader<String> reader, ItemWriter<String> writer,
			ItemProcessor<String, String> processor)
	{
		return stepBuilderFactory.get("dummy").<String, String> chunk(1).reader(reader).processor(processor)
				.writer(writer).listener((StepExecutionListener) monitor())
				.listener((ItemWriteListener<String>) monitor()).build();
	}

	@Autowired
	private JobRepository jobRepository;

	@Bean
	public SimpleJobLauncher asyncJobLauncher()
	{
		SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
		simpleJobLauncher.setJobRepository(jobRepository);
		simpleJobLauncher.setTaskExecutor(asyncTaskExecutor());
		return simpleJobLauncher;
	}

	@Bean
	public TaskExecutor asyncTaskExecutor()
	{
		return new SimpleAsyncTaskExecutor();
	}

	// TODO: run automagically
	private DatabasePopulator createDatabasePopulator()
	{
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.setContinueOnError(true);
		databasePopulator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));
		return databasePopulator;
	}
}
