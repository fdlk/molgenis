package org.molgenis.ui.jobs;

import java.util.List;

import org.springframework.batch.core.StepExecution;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Progress<ResultType>
{
	public abstract StepExecution getStepExecution();

	public abstract List<? extends ResultType> getResult();

	public static <ResultType> Progress<ResultType> create(StepExecution execution, List<? extends ResultType> item)
	{
		return new AutoValue_Progress<ResultType>(execution, item);
	}
}
