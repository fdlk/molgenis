package org.molgenis.data.discovery.request;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by chaopang on 01/03/17.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeMatchCurationRequest.class)
public abstract class AttributeMatchCurationRequest
{
	@NotNull
	public abstract String getTargetAttribute();

	@NotNull
	public abstract List<String> getSourceAttributes();

	@NotNull
	public abstract String getSourceBiobankSampleCollection();

	public static AttributeMatchCurationRequest create(String targetAttribute, List<String> sourceAttributes,
			String sourceBiobankSampleCollection)
	{
		return new AutoValue_AttributeMatchCurationRequest(targetAttribute, sourceAttributes,
				sourceBiobankSampleCollection);
	}
}
