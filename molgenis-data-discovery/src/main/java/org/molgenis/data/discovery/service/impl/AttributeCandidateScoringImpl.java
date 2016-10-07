package org.molgenis.data.discovery.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.IdentifiableTagGroup;
import org.molgenis.data.discovery.model.matching.MatchedAttributeTagGroup;
import org.molgenis.data.discovery.scoring.attributes.AttributeSimilarity;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.utils.NGramDistanceAlgorithm;
import org.molgenis.ontology.utils.Stemmer;

import java.util.*;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.*;

/**
 * This scoring class computes a similarity score between the target {@link BiobankSampleAttribute} and the source
 * {@link BiobankSampleAttribute}.
 *
 * @author chaopang
 */
public class AttributeCandidateScoringImpl
{
	private final OntologyService ontologyService;
	private final AttributeSimilarity attributeSimilarity;
	private final Joiner termJoiner = Joiner.on(' ');

	public AttributeCandidateScoringImpl(OntologyService ontologyService, AttributeSimilarity attributeSimilarity)
	{
		this.ontologyService = requireNonNull(ontologyService);
		this.attributeSimilarity = requireNonNull(attributeSimilarity);
	}

	public Hit<String> score(BiobankSampleAttribute targetAttribute, BiobankSampleAttribute sourceAttribute,
			BiobankUniverse biobankUniverse, Multimap<OntologyTermImpl, OntologyTermImpl> relatedOntologyTermImpls,
			boolean strictMatch)
	{
		List<Hit<String>> allMatchedStrings = new ArrayList<>();

		for (IdentifiableTagGroup targetTagGroup : targetAttribute.getTagGroups())
		{
			for (IdentifiableTagGroup sourceTagGroup : sourceAttribute.getTagGroups())
			{
				Hit<String> calculateScoreForTagPair = calculateScoreForTagPair(targetAttribute, sourceAttribute,
						targetTagGroup, sourceTagGroup, relatedOntologyTermImpls, strictMatch);

				if (Objects.nonNull(calculateScoreForTagPair))
				{
					allMatchedStrings.add(calculateScoreForTagPair);
				}
			}
		}

		Optional<Hit<String>> findFirst = allMatchedStrings.stream().sorted(Comparator.reverseOrder()).findFirst();

		return findFirst.isPresent() ? findFirst.get() : Hit.create(StringUtils.EMPTY, 0.0f);
	}

	Hit<String> calculateScoreForTagPair(BiobankSampleAttribute targetAttribute, BiobankSampleAttribute sourceAttribute,
			IdentifiableTagGroup targetGroup, IdentifiableTagGroup sourceGroup,
			Multimap<OntologyTermImpl, OntologyTermImpl> relatedOntologyTerms, boolean strictMatch)
	{
		List<MatchedAttributeTagGroup> allRelatedOntologyTermTagGroups = new ArrayList<>();

		for (OntologyTermImpl targetOntologyTermImpl : targetGroup.getOntologyTermImpls())
		{
			for (OntologyTermImpl sourceOntologyTermImpl : sourceGroup.getOntologyTermImpls())
			{
				if (relatedOntologyTerms.containsEntry(targetOntologyTermImpl, sourceOntologyTermImpl))
				{
					TagGroup targetOntologyTermTag = createOntologyTermTag(targetOntologyTermImpl, targetAttribute);
					TagGroup sourceOntologyTermTag = createOntologyTermTag(sourceOntologyTermImpl, sourceAttribute);

					Double relatedness = ontologyService
							.getOntologyTermSemanticRelatedness(targetOntologyTermImpl, sourceOntologyTermImpl);

					if (nonNull(targetOntologyTermTag) && nonNull(sourceOntologyTermTag))
					{
						allRelatedOntologyTermTagGroups.add(MatchedAttributeTagGroup
								.create(targetOntologyTermTag, sourceOntologyTermTag, relatedness));
					}
				}
			}
		}

		if (!allRelatedOntologyTermTagGroups.isEmpty())
		{
			Collections.sort(allRelatedOntologyTermTagGroups);

			List<MatchedAttributeTagGroup> filteredRelatedOntologyTermTagGroups = new ArrayList<>();
			List<OntologyTermImpl> occupiedTargetOntologyTerms = new ArrayList<>();
			List<OntologyTermImpl> occupiedSourceOntologyTerms = new ArrayList<>();
			for (MatchedAttributeTagGroup matchedTagGroup : allRelatedOntologyTermTagGroups)
			{
				OntologyTermImpl targetOntologyTermImpl = matchedTagGroup.getTarget().getOntologyTerms().get(0);
				OntologyTermImpl sourceOntologyTermImpl = matchedTagGroup.getSource().getOntologyTerms().get(0);
				if (!occupiedTargetOntologyTerms.contains(targetOntologyTermImpl) && !occupiedSourceOntologyTerms
						.contains(sourceOntologyTermImpl))
				{
					filteredRelatedOntologyTermTagGroups.add(matchedTagGroup);
					occupiedTargetOntologyTerms.add(targetOntologyTermImpl);
					occupiedSourceOntologyTerms.add(sourceOntologyTermImpl);
				}
			}

			String targetLabel = targetAttribute.getLabel();
			String sourceLabel = sourceAttribute.getLabel();

			Hit<String> scoreForLabels = calculate(targetLabel, sourceLabel, filteredRelatedOntologyTermTagGroups,
					strictMatch);

			String targetDescription = targetAttribute.getDescription();
			String sourceDescription = sourceAttribute.getDescription();

			if (isNotBlank(targetDescription) && isNotBlank(sourceDescription) && (
					(!targetLabel.equals(targetDescription) || !sourceLabel.equals(sourceDescription)) && !(
							targetLabel.equals(targetDescription) && sourceLabel.equals(sourceDescription))))
			{
				Hit<String> scoreForDescriptions = calculate(targetDescription, sourceDescription,
						filteredRelatedOntologyTermTagGroups, strictMatch);

				float score =
						scoreForDescriptions.getScore() > scoreForLabels.getScore() ? scoreForDescriptions.getScore() :
								(scoreForLabels.getScore() + scoreForDescriptions.getScore()) / 2;
				scoreForLabels = Hit.create(scoreForLabels.getResult(), score);
			}

			return scoreForLabels;
		}

		return null;
	}

	Hit<String> calculate(String targetLabel, String sourceLabel,
			List<MatchedAttributeTagGroup> filteredRelatedOntologyTerms, boolean strictMatch)
	{
		// Remove the duplicated words from the attribute labels
		targetLabel = termJoiner.join(splitIntoTerms(targetLabel));

		sourceLabel = termJoiner.join(splitIntoTerms(sourceLabel));

		Set<Hit<String>> matchedWords = new HashSet<>();

		Set<String> queryString = new LinkedHashSet<>();

		for (MatchedAttributeTagGroup matchedTagGroup : filteredRelatedOntologyTerms)
		{
			TagGroup targetTagGroup = matchedTagGroup.getTarget();
			TagGroup sourceTagGroup = matchedTagGroup.getSource();

			Set<String> targetMatchedWords = splitIntoUniqueTerms(targetTagGroup.getMatchedWords());
			Set<String> sourceMatchedWords = splitIntoUniqueTerms(sourceTagGroup.getMatchedWords());
			queryString.addAll(targetMatchedWords);
			queryString.addAll(sourceMatchedWords);
			// The source ontologyTerm is more specific therefore we replace it with a more general target
			// ontologyTerm
			if (ontologyService
					.isDescendant(sourceTagGroup.getOntologyTerms().get(0), targetTagGroup.getOntologyTerms().get(0)))
			{
				Set<String> sourceLabelWords = splitIntoUniqueTerms(sourceLabel);
				sourceLabelWords.removeAll(sourceMatchedWords);
				sourceLabel = termJoiner.join(Sets.union(sourceLabelWords, targetMatchedWords));

				matchedWords.add(Hit.create(termJoiner.join(targetMatchedWords),
						matchedTagGroup.getSimilarity().floatValue()));
			}
			else
			{
				Set<String> targetLabelWords = splitIntoUniqueTerms(targetLabel);
				targetLabelWords.removeAll(targetMatchedWords);
				targetLabel = termJoiner.join(Sets.union(targetLabelWords, sourceMatchedWords));

				matchedWords.add(Hit.create(termJoiner.join(sourceMatchedWords),
						matchedTagGroup.getSimilarity().floatValue()));
			}
		}

		float adjustedScore = attributeSimilarity.score(targetLabel, sourceLabel, strictMatch);

		for (Hit<String> matchedWord : matchedWords)
		{
			float ngramContribution =
					adjustedScore * 2 * matchedWord.getResult().length() / (targetLabel.length() + sourceLabel
							.length());
			adjustedScore = adjustedScore - ngramContribution + ngramContribution * (float) Math
					.pow(matchedWord.getScore(), 3.0);
		}

		return Hit.create(termJoiner.join(queryString), adjustedScore);
	}

	private TagGroup createOntologyTermTag(OntologyTermImpl ontologyTerm, BiobankSampleAttribute biobankSampleAttribute)
	{
		Set<String> stemmedAttributeLabelWords = Stemmer.splitAndStem(biobankSampleAttribute.getLabel());

		for (String synonym : ontologyTerm.getSynonyms())
		{
			if (stemmedAttributeLabelWords.containsAll(Stemmer.splitAndStem(synonym)))
			{
				String matchedWords = termJoiner.join(findMatchedWords(biobankSampleAttribute.getLabel(), synonym));
				float score =
						(float) NGramDistanceAlgorithm.stringMatching(biobankSampleAttribute.getLabel(), synonym) / 100;
				return TagGroup.create(ontologyTerm, matchedWords, score);
			}
		}
		return null;
	}
}
