package org.molgenis.data.discovery.model.network;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

@AutoValue
@AutoGson(autoValueClass = AutoValue_VisNetworkRequest.class)
public abstract class VisNetworkRequest
{
	public enum NetworkType
	{
		SEMANTIC_SIMILARITY, CANDIDATE_MATCHES, CURATED_MATCHES;

		private static final Map<String, NetworkType> strValMap;

		static
		{
			NetworkType[] networkTypes = NetworkType.values();
			strValMap = newHashMapWithExpectedSize(networkTypes.length);
			for (NetworkType networkType : networkTypes)
			{
				strValMap.put(networkType.toString().toLowerCase(), networkType);
			}
		}

		public static List<String> getValueStrings()
		{
			return newArrayList(strValMap.keySet());
		}

		public static NetworkType toEnum(String valueString)
		{
			String lowerCase = valueString.toLowerCase();
			return strValMap.containsKey(lowerCase) ? strValMap.get(lowerCase) : SEMANTIC_SIMILARITY;
		}
	}

	public static VisNetworkRequest create(String biobankUniverseIdentifier, String networkType,
			List<String> ontologyTermIris)
	{
		return new AutoValue_VisNetworkRequest(biobankUniverseIdentifier, networkType, ontologyTermIris);
	}

	public abstract String getBiobankUniverseIdentifier();

	public abstract String getNetworkType();

	public abstract List<String> getOntologyTermIris();

	public NetworkType getNetworkTypeEnum()
	{
		return NetworkType.toEnum(getNetworkType());
	}
}
