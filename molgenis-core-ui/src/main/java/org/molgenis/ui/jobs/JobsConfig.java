package org.molgenis.ui.jobs;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	public StepExecutionListener stepListener()
	{
		return new StepExecutionListener()
		{

			@Override
			public void beforeStep(StepExecution stepExecution)
			{
				System.out.println("beforeStep" + stepExecution.getSummary());
			}

			@Override
			public ExitStatus afterStep(StepExecution stepExecution)
			{
				System.out.println("afterStep" + stepExecution.getSummary());
				return stepExecution.getExitStatus();
			}
		};
	}

	@Bean
	public ItemProcessListener<String, String> processListener()
	{
		return new ItemProcessListener<String, String>()
		{

			@Override
			public void beforeProcess(String item)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void afterProcess(String item, String result)
			{

				System.out.println("afterProcess!" + item + result);
			}

			@Override
			public void onProcessError(String item, Exception e)
			{
				// TODO Auto-generated method stub

			}
		};
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<String> reader, ItemWriter<String> writer,
			ItemProcessor<String, String> processor)
	{
		return stepBuilderFactory.get("step1").<String, String> chunk(1).reader(reader).processor(processor)
				.listener(processListener()).writer(writer).listener(stepListener()).build();
	}
}
