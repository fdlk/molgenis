package org.molgenis.ui.jobs;

import java.util.List;

import org.springframework.batch.core.StepExecution;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Progress<ResultType>
{
	public abstract int getWriteCount();

	public abstract long getTotal();

	public abstract List<? extends ResultType> getResult();

	public static <ResultType> Progress<ResultType> create(StepExecution execution, long total,
			List<? extends ResultType> item)
	{
		return new AutoValue_Progress<ResultType>(execution.getWriteCount() + 1, total, item);
	}
}
