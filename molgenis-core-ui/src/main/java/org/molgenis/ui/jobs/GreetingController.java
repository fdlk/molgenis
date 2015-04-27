package org.molgenis.ui.jobs;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GreetingController
{
	ExecutorService executorService = Executors.newSingleThreadExecutor();
	private SimpMessagingTemplate template;
	@Autowired
	private JobRepository jobRepository;

	@Autowired
	public GreetingController(SimpMessagingTemplate template)
	{
		this.template = template;
	}

	@RequestMapping(value = "/submit", method = POST, consumes = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<String> startJobs(@RequestBody HelloMessage message)
	{

		System.out.println("startJobs" + message);
		executorService.submit(() -> greet("spinner1"));
		executorService.submit(() -> greet("spinner2"));
		executorService.submit(() -> greet("spinner3"));
		return Arrays.asList("spinner1", "spinner2", "spinner3");
	}

	public Void greet(String name) throws InterruptedException
	{
		Thread.sleep(3000);
		System.out.println("greet:" + name);
		template.convertAndSend("/topic/greetings", Greeting.create(name));
		return null;
	}
}
