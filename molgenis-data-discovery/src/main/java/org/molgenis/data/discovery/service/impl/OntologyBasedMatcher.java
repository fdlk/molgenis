package org.molgenis.data.discovery.service.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.repo.BiobankUniverseRepository;
import org.molgenis.data.semanticsearch.service.QueryExpansionService;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.discovery.meta.biobank.BiobankSampleAttributeMetaData.IDENTIFIER;

public class OntologyBasedMatcher
{
	public static final int STOP_LEVEL = 4;
	public static final int EXPANSION_LEVEL = 5;
	public static final int MAX_NUMBER_LEXICAL_MATCHES = 20;

	private static final Logger LOG = LoggerFactory.getLogger(OntologyBasedMatcher.class);
	private static final String ESCAPED_NODEPATH_SEPARATOR = "\\.";
	private static final String NODEPATH_SEPARATOR = ".";

	private final BiobankUniverseRepository biobankUniverseRepository;
	private final QueryExpansionService queryExpansionService;

	private final Iterable<BiobankSampleAttribute> biobankSampleAttributes;
	private final Multimap<String, BiobankSampleAttribute> nodePathRegistry;
	private final Multimap<String, BiobankSampleAttribute> descendantNodePathsRegistry;
	private final Map<OntologyTermImpl, List<BiobankSampleAttribute>> cachedBiobankSampleAttributes;

	public OntologyBasedMatcher(BiobankSampleCollection biobankSampleCollection,
			BiobankUniverseRepository biobankUniverseRepository, QueryExpansionService queryExpansionService)
	{
		this(biobankUniverseRepository.getBiobankSampleAttributes(biobankSampleCollection), biobankUniverseRepository,
				queryExpansionService);
	}

	public OntologyBasedMatcher(List<BiobankSampleAttribute> biobankSampleAttributes,
			BiobankUniverseRepository biobankUniverseRepository, QueryExpansionService queryExpansionService)
	{
		this.nodePathRegistry = LinkedHashMultimap.create();
		this.descendantNodePathsRegistry = LinkedHashMultimap.create();
		this.biobankUniverseRepository = requireNonNull(biobankUniverseRepository);
		this.queryExpansionService = requireNonNull(queryExpansionService);
		this.biobankSampleAttributes = requireNonNull(biobankSampleAttributes);
		this.cachedBiobankSampleAttributes = new HashMap<>();
		constructTree();
	}

	public List<BiobankSampleAttribute> match(SearchParam searchParam)
	{
		Set<BiobankSampleAttribute> matchedSourceAttribtues = new LinkedHashSet<>();

		LOG.trace("Started lexical match...");

		// Lexical match
		matchedSourceAttribtues.addAll(lexicalSearchBiobankSampleAttributes(searchParam));

		// Semantic match
		List<BiobankSampleAttribute> semanticMatches = searchParam.getTagGroups().stream()
				.flatMap(tagGroup -> tagGroup.getOntologyTerms().stream()).distinct()
				.flatMap(ontologyTerm -> semanticSearchBiobankSampleAttributes(ontologyTerm).stream())
				.collect(toList());

		LOG.trace("Finished semantic match...");

		matchedSourceAttribtues.addAll(semanticMatches);

		return Lists.newArrayList(matchedSourceAttribtues);
	}

	List<BiobankSampleAttribute> lexicalSearchBiobankSampleAttributes(SearchParam searchParam)
	{
		List<BiobankSampleAttribute> matches = new ArrayList<>();

		QueryRule expandedQuery = queryExpansionService.expand(searchParam);

		if (expandedQuery != null)
		{
			List<String> identifiers = StreamSupport.stream(biobankSampleAttributes.spliterator(), false)
					.map(BiobankSampleAttribute::getIdentifier).collect(Collectors.toList());

			List<QueryRule> finalQueryRules = Lists.newArrayList(new QueryRule(IDENTIFIER, IN, identifiers));

			if (expandedQuery.getNestedRules().size() > 0)
			{
				finalQueryRules.addAll(asList(new QueryRule(AND), expandedQuery));
			}

			List<BiobankSampleAttribute> lexicalMatches = biobankUniverseRepository.queryBiobankSampleAttribute(
					new QueryImpl<Entity>(finalQueryRules).pageSize(MAX_NUMBER_LEXICAL_MATCHES))
					.collect(Collectors.toList());

			LOG.trace("Finished lexical match...");
			LOG.trace("Started semantic match...");

			matches.addAll(lexicalMatches);
		}

		return matches;
	}

	List<BiobankSampleAttribute> semanticSearchBiobankSampleAttributes(OntologyTermImpl ontologyTermImpl)
	{
		List<BiobankSampleAttribute> candidates = new ArrayList<>();

		if (cachedBiobankSampleAttributes.containsKey(ontologyTermImpl))
		{
			candidates.addAll(cachedBiobankSampleAttributes.get(ontologyTermImpl));
		}
		else
		{
			for (String nodePath : ontologyTermImpl.getNodePaths())
			{
				// if a direct hit for the current nodePath is found, we want to get all the associated
				// BiobankSampleAttributes from the descendant nodePaths.
				if (descendantNodePathsRegistry.containsKey(nodePath))
				{
					candidates.addAll(descendantNodePathsRegistry.get(nodePath));
				}
				// if a hit for the parent nodePath is found, we only want to get associated BiobankSampleAttributes
				// from that particular parent nodePath
				List<BiobankSampleAttribute> collect = StreamSupport
						.stream(getAllParents(nodePath).spliterator(), false).limit(EXPANSION_LEVEL)
						.filter(parentNodePath -> getNodePathLevel(parentNodePath) > STOP_LEVEL)
						.filter(nodePathRegistry::containsKey)
						.flatMap(parentNodePath -> nodePathRegistry.get(parentNodePath).stream()).distinct()
						.collect(Collectors.toList());

				candidates.addAll(collect);
			}

			cachedBiobankSampleAttributes.put(ontologyTermImpl, candidates);
		}

		return candidates;
	}

	private void constructTree()
	{
		LOG.trace("Starting to construct the tree...");

		for (BiobankSampleAttribute biobankSampleAttribute : biobankSampleAttributes)
		{
			biobankSampleAttribute.getTagGroups().stream().flatMap(tagGroup -> tagGroup.getOntologyTerms().stream())
					.distinct().flatMap(ot -> ot.getNodePaths().stream()).forEach(nodePath ->
			{

				// Register the direct association between nodePaths and BiobankSampleAttributes
				nodePathRegistry.put(nodePath, biobankSampleAttribute);

				if (getNodePathLevel(nodePath) > STOP_LEVEL)
				{
					// Register the direct associations plus the descendant associations between nodePaths and
					// BiobankSampleAttributes
					descendantNodePathsRegistry.put(nodePath, biobankSampleAttribute);

					for (String parentNodePath : stream(getAllParents(nodePath).spliterator(), false)
							.limit(EXPANSION_LEVEL).collect(toList()))
					{
						if (getNodePathLevel(parentNodePath) > STOP_LEVEL)
						{
							descendantNodePathsRegistry.put(parentNodePath, biobankSampleAttribute);
						}
						else break;
					}
				}
			});
		}

		LOG.trace("Finished constructing the tree...");
	}

	int getNodePathLevel(String nodePath)
	{
		return nodePath.split(ESCAPED_NODEPATH_SEPARATOR).length;
	}

	Iterable<String> getAllParents(String nodePath)
	{
		return new Iterable<String>()
		{
			final String[] split = nodePath.split(ESCAPED_NODEPATH_SEPARATOR);
			private int size = split.length;

			public Iterator<String> iterator()
			{
				return new Iterator<String>()
				{
					@Override
					public boolean hasNext()
					{
						return size > 1;
					}

					@Override
					public String next()
					{
						String parent = Stream.of(Arrays.copyOf(split, --size)).collect(joining(NODEPATH_SEPARATOR));
						return parent;
					}
				};
			}
		};
	}
}