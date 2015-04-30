package org.molgenis.ui.jobs;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.StepExecution;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Progress<ResultType>
{
	public abstract long getWriteCount();

	public abstract long getTotal();

	public abstract boolean isDone();

	public abstract List<? extends ResultType> getResult();

	public static <ResultType> Progress<ResultType> create(StepExecution execution, long total,
			List<? extends ResultType> item)
	{
		return new AutoValue_Progress<ResultType>(execution.getWriteCount() + 1, total, false, item);
	}

	public static <ResultType> Progress<ResultType> createDone(long total)
	{
		return new AutoValue_Progress<ResultType>(total, total, false, Collections.emptyList());
	}
}
