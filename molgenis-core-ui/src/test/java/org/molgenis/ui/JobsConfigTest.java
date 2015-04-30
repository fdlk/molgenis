package org.molgenis.ui;

import org.molgenis.ui.jobs.JobsConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ JobsConfig.class })
public class JobsConfigTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private JobRepository jobRepository;

	@Autowired
	FlowJobBuilder builder;

	@BeforeTest
	public void beforeTest()
	{

	}

	@Test
	public void test() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, InterruptedException
	{
		Job sleepJob = builder.build();
		JobParameters params = new JobParameters();
		JobExecution execution = jobRepository.createJobExecution("sleepJob", params);
		sleepJob.execute(execution);
		System.out.println(execution.isRunning());
		Thread.sleep(10000);
	}
}
