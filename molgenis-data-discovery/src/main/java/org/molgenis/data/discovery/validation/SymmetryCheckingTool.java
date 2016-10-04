package org.molgenis.data.discovery.validation;

import static java.util.Collections.emptyList;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.springframework.beans.factory.annotation.Autowired;

public class SymmetryCheckingTool
{
	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	public static void main(String args[]) throws IOException, MolgenisInvalidFormatException
	{
		if (args.length == 2)
		{
			File forwardMatchFile = new File(args[0]);
			File reversedMatchFile = new File(args[1]);
			if (forwardMatchFile.exists() && reversedMatchFile.exists())
			{
				SymmetryCheckingTool tool = new SymmetryCheckingTool();
				tool.compare(forwardMatchFile, reversedMatchFile);
			}
		}
	}

	private void compare(File forwardMatchFile, File reversedMatchFile)
	{
		Repository<Entity> forwardGeneratedMatchRepository = new CsvRepository(forwardMatchFile, entityMetaDataFactory,
				attributeMetaDataFactory, emptyList(), ',');
		Repository<Entity> backwordGeneratedMatchRepository = new CsvRepository(reversedMatchFile,
				entityMetaDataFactory, attributeMetaDataFactory, emptyList(), ',');

		Map<String, List<Hit<String>>> forwardGeneratedMatches = BiobankUniverseEvaluationTool
				.collectGeneratedMatches(forwardGeneratedMatchRepository, false);

		Map<String, List<Hit<String>>> backwardGeneratedMatches = BiobankUniverseEvaluationTool
				.collectGeneratedMatches(backwordGeneratedMatchRepository, true);

		Set<String> targetAttributes = new HashSet<>();
		targetAttributes.addAll(forwardGeneratedMatches.keySet());
		targetAttributes.addAll(backwardGeneratedMatches.keySet());

		for (String targetAttribute : targetAttributes)
		{
			List<String> forwardCandidateMatches = forwardGeneratedMatches.containsKey(targetAttribute)
					? forwardGeneratedMatches.get(targetAttribute).stream().map(Hit::getResult)
							.collect(Collectors.toList())
					: Collections.emptyList();

			List<String> backwardCandidateMatches = backwardGeneratedMatches.containsKey(targetAttribute)
					? backwardGeneratedMatches.get(targetAttribute).stream().map(Hit::getResult)
							.collect(Collectors.toList())
					: Collections.emptyList();

			List<String> overlapCandidateMatches = forwardCandidateMatches.stream()
					.filter(backwardCandidateMatches::contains).collect(Collectors.toList());

			forwardCandidateMatches.removeAll(overlapCandidateMatches);
			backwardCandidateMatches.removeAll(overlapCandidateMatches);

			if (!forwardCandidateMatches.isEmpty() || !backwardCandidateMatches.isEmpty())
			{
				System.out.format("The target: %s; The forward difference: %s; The backward difference: %s%n",
						targetAttribute, forwardCandidateMatches, backwardCandidateMatches);
			}
		}
	}
}
