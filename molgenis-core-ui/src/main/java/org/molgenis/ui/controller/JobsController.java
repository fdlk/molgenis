package org.molgenis.ui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ui.jobs.HelloMessage;
import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(JobsController.URI)
public class JobsController extends MolgenisPluginController
{
	public static final String ID = "jobs";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public JobsController()
	{
		super(URI);
	}

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job job;

	@Autowired
	private JobService jobs;
	@Autowired
	private JobRepository jobRepository;

	@RequestMapping(method = RequestMethod.GET)
	public String listJobs(Model model) throws NoSuchJobException
	{
		List<JobExecution> allExecutions = Lists.newArrayList();
		System.out.println("jobs count:" + jobs.countJobs());
		for (String jobName : jobs.listJobs(0, jobs.countJobs()))
		{
			int count = jobs.countJobExecutionsForJob(jobName);
			int from = Math.max(0, count - 10);
			allExecutions.addAll(jobs.listJobExecutionsForJob(jobName, from, count - from));
		}
		model.addAttribute("executions", allExecutions);
		return "view-jobs-list";
	}

	@RequestMapping(value = "/submit", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody String submit(@RequestBody HelloMessage message) throws JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobException
	{

		System.out.println("submit!");
		JobParameters params = job.getJobParametersIncrementer().getNext(jobs.getLastJobParameters(job.getName()));
		JobExecution execution = jobLauncher.run(job, params);
		System.out.println(execution);
		return job.getName();
	}

}
