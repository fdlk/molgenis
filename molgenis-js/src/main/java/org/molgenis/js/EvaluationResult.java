package org.molgenis.js;

import javax.annotation.Nullable;

import org.molgenis.data.Entity;

import com.google.auto.value.AutoValue;

/**
 * Result of applying algorithm to one source entity row
 */
@AutoValue
public abstract class EvaluationResult
{
	@Nullable
	public abstract Object getValue();

	@Nullable
	public abstract RuntimeException getException();

	public abstract Entity getSourceEntity();

	public boolean isSuccess()
	{
		return getException() == null;
	}

	public static EvaluationResult createSuccess(Object object, Entity sourceEntity)
	{
		return new AutoValue_EvaluationResult(object, null, sourceEntity);
	}

	public static EvaluationResult createFailure(RuntimeException e, Entity sourceEntity)
	{
		return new AutoValue_EvaluationResult(null, e, sourceEntity);
	}
}