package org.molgenis.ui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Date;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ui.jobs.HelloMessage;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

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
	SimpleJobLauncher asyncJobLauncher;

	@Autowired
	Job job;

	@Autowired
	private JobExplorer jobs;

	@RequestMapping(method = RequestMethod.GET)
	public String listJobs(Model model) throws NoSuchJobException
	{
		model.addAttribute("jobs", jobs);
		return "view-jobs-list";
	}

	// Launches dummy job for the spinner proof of concept
	@RequestMapping(value = "/submit", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody long submit(@RequestBody HelloMessage message) throws JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobException
	{
		JobParameters params = new JobParameters(ImmutableMap.<String, JobParameter> of("total", new JobParameter(3L),
				"date", new JobParameter(new Date())));
		JobExecution execution = asyncJobLauncher.run(job, params);
		return execution.getJobInstance().getInstanceId();
	}
}
