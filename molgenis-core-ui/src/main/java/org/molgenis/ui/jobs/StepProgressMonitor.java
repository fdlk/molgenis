package org.molgenis.ui.jobs;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Monitors Job Progress and reports on a {@link SimpMessagingTemplate}
 * 
 * @author fkelpin
 *
 * @param <T>
 *            the result item type
 */
public class StepProgressMonitor<T> implements ItemWriteListener<T>, StepExecutionListener
{
	private StepExecution stepExecution;
	private SimpMessagingTemplate template;
	private String destination;

	@Override
	public void beforeWrite(List<? extends T> items)
	{

	}

	@Override
	public void afterWrite(List<? extends T> items)
	{
		Progress<T> progress = Progress.<T> create(stepExecution, items);
		template.convertAndSend(destination, progress);
	}

	@Override
	public void onWriteError(Exception exception, List<? extends T> items)
	{

	}

	@Override
	public void beforeStep(StepExecution stepExecution)
	{
		this.stepExecution = stepExecution;
		this.destination = String.format("jobs/%s/%s", stepExecution.getJobExecutionId(), stepExecution.getStepName());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution)
	{
		return stepExecution.getExitStatus();
	}

}
