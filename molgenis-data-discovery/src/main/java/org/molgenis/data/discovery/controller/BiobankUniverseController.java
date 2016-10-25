package org.molgenis.data.discovery.controller;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.discovery.job.BiobankUniverseJobExecution;
import org.molgenis.data.discovery.job.BiobankUniverseJobExecutionMetaData;
import org.molgenis.data.discovery.job.BiobankUniverseJobFactory;
import org.molgenis.data.discovery.job.BiobankUniverseJobImpl;
import org.molgenis.data.discovery.meta.biobank.BiobankSampleCollectionMetaData;
import org.molgenis.data.discovery.meta.biobank.BiobankUniverseMetaData;
import org.molgenis.data.discovery.model.biobank.BiobankSampleAttribute;
import org.molgenis.data.discovery.model.biobank.BiobankSampleCollection;
import org.molgenis.data.discovery.model.biobank.BiobankUniverse;
import org.molgenis.data.discovery.model.matching.AttributeMappingCandidate;
import org.molgenis.data.discovery.model.matching.BiobankSampleCollectionSimilarity;
import org.molgenis.data.discovery.model.network.VisEdge;
import org.molgenis.data.discovery.model.network.VisNetworkRequest;
import org.molgenis.data.discovery.model.network.VisNetworkResponse;
import org.molgenis.data.discovery.model.network.VisNode;
import org.molgenis.data.discovery.repo.BiobankUniverseRepository;
import org.molgenis.data.discovery.service.BiobankUniverseService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semanticsearch.service.QueryExpansionService;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.file.FileStore;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverse;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.molgenis.data.discovery.controller.BiobankUniverseController.URI;
import static org.molgenis.data.discovery.job.BiobankUniverseJobExecutionMetaData.BIOBANK_UNIVERSE_JOB_EXECUTION;
import static org.molgenis.data.discovery.meta.matching.AttributeMappingDecisionMetaData.DecisionOptions.YES;
import static org.molgenis.data.discovery.model.network.NetworkConfiguration.NODE_SHAPE;
import static org.molgenis.data.discovery.model.network.VisNetworkRequest.NetworkType.getValueStrings;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class BiobankUniverseController extends MolgenisPluginController
{
	private final TagGroupGenerator tagGroupGenerator;
	private final DataService dataService;
	private final BiobankUniverseJobFactory biobankUniverseJobFactory;
	private final ExecutorService taskExecutor;
	private final BiobankUniverseService biobankUniverseService;
	private final OntologyService ontologyService;
	private final UserAccountService userAccountService;
	private final FileStore fileStore;
	private final EntityManager entityManager;
	private final IdGenerator idGenerator;
	private final BiobankUniverseJobExecutionMetaData biobankUniverseJobExecutionMetaData;
	private final BiobankUniverseMetaData biobankUniverseMetaData;
	private final BiobankSampleCollectionMetaData biobankSampleCollectionMetaData;
	private final MenuReaderService menuReaderService;

	public static final String VIEW_BIOBANK_UNIVERSES = "view-biobank-universes";
	public static final String VIEW_SINGLE_BIOBANK_UNIVERSE = "view-single-biobank-universe";
	public static final String VIEW_BIOBANK_UNIVERSE_NETWORK = "view-biobank-universe-network";
	public static final String VIEW_BIOBANK_UNIVERSE_CURATE = "view-biobank-universe-curate";
	public static final String ID = "biobankuniverse";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	public BiobankUniverseController(TagGroupGenerator tagGroupGenerator,
			BiobankUniverseJobFactory biobankUniverseJobFactory, BiobankUniverseService biobankUniverseService,
			OntologyService ontologyService, ExecutorService taskExecutor, UserAccountService userAccountService,
			DataService dataService, FileStore fileStore, QueryExpansionService queryExpansionService,
			BiobankUniverseRepository biobankUniverseRepository, LanguageService languageService,
			EntityManager entityManager, IdGenerator idGenerator,
			BiobankUniverseJobExecutionMetaData biobankUniverseJobExecutionMetaData,
			BiobankUniverseMetaData biobankUniverseMetaData,
			BiobankSampleCollectionMetaData biobankSampleCollectionMetaData, MolgenisUserMetaData molgenisUserMetaData,
			MenuReaderService menuReaderService)
	{
		super(URI);
		this.tagGroupGenerator = requireNonNull(tagGroupGenerator);
		this.biobankUniverseService = requireNonNull(biobankUniverseService);
		this.ontologyService = requireNonNull(ontologyService);
		this.biobankUniverseJobFactory = requireNonNull(biobankUniverseJobFactory);
		this.taskExecutor = requireNonNull(taskExecutor);
		this.dataService = requireNonNull(dataService);
		this.fileStore = requireNonNull(fileStore);
		this.userAccountService = requireNonNull(userAccountService);
		this.entityManager = requireNonNull(entityManager);
		this.idGenerator = requireNonNull(idGenerator);
		this.biobankUniverseJobExecutionMetaData = requireNonNull(biobankUniverseJobExecutionMetaData);
		this.biobankUniverseMetaData = requireNonNull(biobankUniverseMetaData);
		this.biobankSampleCollectionMetaData = requireNonNull(biobankSampleCollectionMetaData);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("biobankUniverses", getUserBiobankUniverses());
		model.addAttribute("biobankSampleCollections", getBiobankSampleCollecitons());
		model.addAttribute("semanticTypeGroups", getSemanticTypes());
		return VIEW_BIOBANK_UNIVERSES;
	}

	@RequestMapping(method = POST, value = "/universe/curate/{id}")
	public String curateMatches(@PathVariable("id") String identifier,
			@RequestParam(value = "targetAttribute") String targetAttributeIdentifier,
			@RequestParam(value = "sourceAttributes") String sourceAttributeIdentifiers,
			@RequestParam(value = "targetSampleCollection") String targetSampleCollectionName,
			@RequestParam(value = "sourceSampleCollection") String sourceSampleCollectionName, Model model)
	{
		if (isNotBlank(identifier) && isNotBlank(targetAttributeIdentifier) && isNotBlank(sourceAttributeIdentifiers)
				&& isNotBlank(targetSampleCollectionName) && isNotBlank(sourceSampleCollectionName))
		{
			BiobankUniverse biobankUniverse = biobankUniverseService.getBiobankUniverse(identifier);
			if (nonNull(biobankUniverse))
			{
				BiobankSampleAttribute targetAttrinute = biobankUniverseService
						.getBiobankSampleAttribute(targetAttributeIdentifier);

				List<BiobankSampleAttribute> sourceSampleAttributes = Stream.of(sourceAttributeIdentifiers.split(","))
						.map(StringUtils::trim).map(biobankUniverseService::getBiobankSampleAttribute)
						.collect(toList());

				BiobankSampleCollection targetSampleCollection = biobankUniverseService
						.getBiobankSampleCollection(targetSampleCollectionName);
				BiobankSampleCollection sourceSampleCollection = biobankUniverseService
						.getBiobankSampleCollection(sourceSampleCollectionName);

				MolgenisUser currentUser = userAccountService.getCurrentUser();

				biobankUniverseService.curateAttributeMappingCandidates(biobankUniverse, targetAttrinute, sourceSampleAttributes,
						targetSampleCollection, sourceSampleCollection, currentUser);
			}
		}

		return "redirect:" + getMappingServiceMenuUrl() + "/universe/" + identifier + "?targetSampleCollectionName="
				+ targetSampleCollectionName;
	}

	@RequestMapping("/universe/{id}")
	public String getUniverse(@PathVariable("id") String identifier,
			@RequestParam(value = "targetSampleCollectionName", required = false) String targetSampleCollectionName,
			Model model)
	{
		if (isNotBlank(identifier))
		{
			BiobankUniverse biobankUniverse = biobankUniverseService.getBiobankUniverse(identifier);

			if (nonNull(biobankUniverse) && biobankUniverse.getMembers().size() > 1)
			{
				List<BiobankSampleCollection> members = biobankUniverse.getMembers().stream().skip(0).collect(toList());
				reverse(members);

				BiobankSampleCollection biobankSampleCollection;
				if (isNotBlank(targetSampleCollectionName))
				{
					biobankSampleCollection = biobankUniverseService
							.getBiobankSampleCollection(targetSampleCollectionName);
				}
				else
				{
					biobankSampleCollection = members.get(0);
				}

				Table<BiobankSampleAttribute, BiobankSampleCollection, List<AttributeMappingCandidate>> candidateMappingCandidates = biobankUniverseService
						.getCandidateMappingsCandidates(biobankUniverse, biobankSampleCollection);

				Map<String, Map<String, List<AttributeMappingCandidate>>> candidateMappingCandidatesFreemarker = new HashMap<>();
				Map<String, Map<String, Boolean>> candidateMappingCandidatesDecision = new HashMap<>();
				Map<String, BiobankSampleAttribute> biobankSampleAttributeMap = new HashMap<>();
				candidateMappingCandidates.cellSet().forEach(cell ->
				{
					String attributeName = cell.getRowKey().getName();
					String collectionName = cell.getColumnKey().getName();

					if (!biobankSampleAttributeMap.containsKey(attributeName))
					{
						biobankSampleAttributeMap.put(attributeName, cell.getRowKey());
					}

					if (!candidateMappingCandidatesFreemarker.containsKey(attributeName))
					{
						candidateMappingCandidatesFreemarker.put(attributeName, new HashMap<>());
					}

					if (!candidateMappingCandidatesFreemarker.get(attributeName).containsKey(collectionName))
					{
						candidateMappingCandidatesFreemarker.get(attributeName).put(collectionName, cell.getValue());
					}

					if (!candidateMappingCandidatesDecision.containsKey(attributeName))
					{
						candidateMappingCandidatesDecision.put(attributeName, new HashMap<>());
					}

					if (!candidateMappingCandidatesDecision.get(attributeName).containsKey(collectionName))
					{
						boolean curated = cell.getValue().stream()
								.flatMap(candidate -> candidate.getDecisions().stream())
								.anyMatch(decision -> decision.getDecision().equals(YES));

						candidateMappingCandidatesDecision.get(attributeName).put(collectionName, curated);
					}
				});

				model.addAttribute("biobankUniverse", biobankUniverse);
				model.addAttribute("sampleCollections", members);
				model.addAttribute("targetSampleCollection", biobankSampleCollection);
				model.addAttribute("candidateMappingCandidates", candidateMappingCandidatesFreemarker);
				model.addAttribute("candidateMappingCandidatesDecision", candidateMappingCandidatesDecision);
				model.addAttribute("biobankSampleAttributeMap", biobankSampleAttributeMap);
			}
		}

		return VIEW_BIOBANK_UNIVERSE_CURATE;
	}

	@RequestMapping("/universe/tag")
	@ResponseBody
	public List<TagGroup> tag(@RequestBody Map<String, String> request)
	{
		String queryString = request.get("queryString");
		if (isNotBlank(queryString))
		{
			return tagGroupGenerator.generateTagGroups(queryString, ontologyService.getAllOntologyIds());
		}
		return Collections.emptyList();
	}

	@RequestMapping(method = GET, value = "/network")
	public String network(Model model)
	{
		model.addAttribute("biobankSampleCollections", biobankUniverseService.getAllBiobankSampleCollections());
		model.addAttribute("biobankUniverses", biobankUniverseService.getBiobankUniverses());
		model.addAttribute("networkTypes", getValueStrings());

		return VIEW_BIOBANK_UNIVERSE_NETWORK;
	}

	//	@RequestMapping(method = GET, value = "/network/matrix")
	//	public String upload(@RequestParam(value = "biobankUniverseIdentifier", required = true) String identifier,
	//			Model model) throws Exception
	//	{
	//		BiobankUniverse biobankUniverse = biobankUniverseService.getBiobankUniverse(identifier);
	//
	//		Map<String, List<BiobankSampleCollectionSimilarity>> collectionSimilarityMap = new LinkedHashMap<>();
	//
	//		if (Objects.nonNull(biobankUniverse))
	//		{
	//			List<BiobankSampleCollectionSimilarity> collectionSimilarities = biobankUniverseService
	//					.getCollectionSimilarities(biobankUniverse);
	//
	//			List<BiobankSampleCollection> collect = collectionSimilarities.stream().flatMap(
	//					collectionSimilarity -> Stream
	//							.of(collectionSimilarity.getTarget(), collectionSimilarity.getSource())).distinct()
	//					.collect(Collectors.toList());
	//
	//			float[][] similarities = new float[collect.size()][collect.size()];
	//
	//			for (BiobankSampleCollectionSimilarity collectionSimilarity : collectionSimilarities)
	//			{
	//				BiobankSampleCollection target = collectionSimilarity.getTarget();
	//				BiobankSampleCollection source = collectionSimilarity.getSource();
	//
	//				int rowIndex = collect.indexOf(target);
	//				int colIndex = collect.indexOf(source);
	//
	//				similarities[rowIndex][colIndex] = collectionSimilarity.getSimilarity();
	//			}
	//
	//			for (int rowIndex = 0; rowIndex < similarities.length; rowIndex++)
	//			{
	//				BiobankSampleCollection target = collect.get(rowIndex);
	//				collectionSimilarityMap.put(target.getName(), new ArrayList<>());
	//				for (int colIndex = 0; colIndex < similarities[rowIndex].length; colIndex++)
	//				{
	//					BiobankSampleCollection source = collect.get(colIndex);
	//					float similarity = similarities[rowIndex][colIndex];
	//					collectionSimilarityMap.get(target.getName())
	//							.add(BiobankSampleCollectionSimilarity.create(target, source, similarity));
	//				}
	//			}
	//		}
	//
	//		model.addAttribute("semanticSimilarityMap", collectionSimilarityMap);
	//
	//		return test(model);
	//	}

	@RequestMapping(method = POST, value = "/network/create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public VisNetworkResponse createNetwork(@RequestBody VisNetworkRequest visNetworkRequest)
	{
		List<VisNode> nodes = new ArrayList<>();

		List<VisEdge> edges = new ArrayList<>();

		BiobankUniverse biobankUniverse = biobankUniverseService
				.getBiobankUniverse(visNetworkRequest.getBiobankUniverseIdentifier());

		if (Objects.nonNull(biobankUniverse))
		{
			List<BiobankSampleCollectionSimilarity> collectionSimilarities = biobankUniverseService
					.getCollectionSimilarities(biobankUniverse, visNetworkRequest.getNetworkTypeEnum());

			List<BiobankSampleCollection> uniqueBiobankCollections = collectionSimilarities.stream().flatMap(
					collectionSimilarity -> Stream
							.of(collectionSimilarity.getTarget(), collectionSimilarity.getSource())).distinct()
					.collect(toList());

			for (BiobankSampleCollection biobankSampleCollection : uniqueBiobankCollections)
			{
				int size = biobankUniverseService.countBiobankSampleAttributes(biobankSampleCollection);
				nodes.add(VisNode.create(biobankSampleCollection.getName(), biobankSampleCollection.getName(), size,
						NODE_SHAPE));
			}

			Multimap<String, String> existingPairs = LinkedHashMultimap.create();
			for (BiobankSampleCollectionSimilarity biobankCollectionSimilarity : collectionSimilarities)
			{
				String targetName = biobankCollectionSimilarity.getTarget().getName();
				String sourceName = biobankCollectionSimilarity.getSource().getName();
				String label = biobankCollectionSimilarity.getLabel();

				if (!existingPairs.containsEntry(targetName, sourceName) && !existingPairs
						.containsEntry(sourceName, targetName))
				{
					String identifier = targetName + sourceName;
					double distance = Math.round(biobankCollectionSimilarity.getSimilarity() * 100.0) / 100.0;
					edges.add(VisEdge.create(identifier, label, distance, targetName, sourceName));
					existingPairs.put(targetName, sourceName);
				}
			}
		}

		return VisNetworkResponse.create(nodes, edges);
	}

	@RequestMapping(value = "/addUniverse", method = RequestMethod.POST)
	public String addUniverse(@RequestParam("universeName") String universeName,
			@RequestParam(required = false) String[] biobankSampleCollectionNames,
			@RequestParam(required = false) String[] semanticTypes, Model model)
	{
		if (isNotBlank(universeName))
		{
			BiobankUniverse biobankUniverse = biobankUniverseService.addBiobankUniverse(universeName,
					semanticTypes != null ? of(semanticTypes).collect(toList()) : emptyList(),
					userAccountService.getCurrentUser());

			if (biobankSampleCollectionNames != null)
			{
				List<BiobankSampleCollection> biobankSampleCollections = biobankUniverseService
						.getBiobankSampleCollections(of(biobankSampleCollectionNames).collect(toList()));

				submit(biobankUniverse, biobankSampleCollections);
			}
		}

		model.addAttribute("biobankUniverses", getUserBiobankUniverses());
		model.addAttribute("biobankSampleCollections", getBiobankSampleCollecitons());
		model.addAttribute("semanticTypeGroups", getSemanticTypes());

		return init(model);
	}

	@RequestMapping(value = "/removeBiobankuniverse", method = RequestMethod.POST)
	public String deleteBiobankUniverse(@RequestParam(required = true) String biobankUniverseId, Model model)
	{
		biobankUniverseService.deleteBiobankUniverse(biobankUniverseId);
		model.addAttribute("biobankUniverses", getUserBiobankUniverses());
		model.addAttribute("biobankSampleCollections", getBiobankSampleCollecitons());
		model.addAttribute("semanticTypeGroups", getSemanticTypes());
		return init(model);
	}

	@RequestMapping(value = "/addUniverseMembers", method = POST)
	public String addMembers(@RequestParam(required = true) String biobankUniverseId,
			@RequestParam(required = false) String[] biobankSampleCollectionNames, Model model)
	{
		BiobankUniverse biobankUniverse = biobankUniverseService.getBiobankUniverse(biobankUniverseId);

		if (biobankUniverse != null && biobankSampleCollectionNames != null)
		{
			List<BiobankSampleCollection> biobankSampleCollections = biobankUniverseService
					.getBiobankSampleCollections(Stream.of(biobankSampleCollectionNames).collect(toList()));

			submit(biobankUniverse, biobankSampleCollections);
		}

		return init(model);
	}

	@RequestMapping(value = "/addKeyConcepts", method = POST)
	public String addKeyConcepts(@RequestParam(required = true) String biobankUniverseId,
			@RequestParam(required = false) String[] semanticTypes, Model model)
	{
		BiobankUniverse universe = biobankUniverseService.getBiobankUniverse(biobankUniverseId);

		biobankUniverseService
				.addKeyConcepts(universe, semanticTypes != null ? of(semanticTypes).collect(toList()) : emptyList());

		model.addAttribute("biobankUniverses", getUserBiobankUniverses());
		model.addAttribute("biobankSampleCollections", getBiobankSampleCollecitons());
		model.addAttribute("semanticTypeGroups", getSemanticTypes());

		return init(model);
	}

	private void submit(BiobankUniverse biobankUniverse, List<BiobankSampleCollection> biobankSampleCollections)
	{
		List<BiobankSampleCollection> newMembers = biobankSampleCollections.stream()
				.filter(member -> !biobankUniverse.getMembers().contains(member)).collect(toList());

		// Add new members to the universe
		biobankUniverseService.addBiobankUniverseMember(biobankUniverse, newMembers);

		BiobankUniverseJobExecution jobExecution = new BiobankUniverseJobExecution(biobankUniverseJobExecutionMetaData,
				biobankUniverseMetaData, biobankSampleCollectionMetaData, biobankUniverseService, entityManager);
		jobExecution.setIdentifier(idGenerator.generateId());
		jobExecution.setUniverse(biobankUniverse);
		jobExecution.setMembers(newMembers);
		jobExecution.setUser(userAccountService.getCurrentUser());

		RunAsSystemProxy.runAsSystem(() ->
		{
			dataService.add(BIOBANK_UNIVERSE_JOB_EXECUTION, jobExecution);
		});

		BiobankUniverseJobImpl biobankUniverseJobImpl = biobankUniverseJobFactory.create(jobExecution);
		taskExecutor.submit(biobankUniverseJobImpl);
	}

	private Set<String> getSemanticTypes()
	{
		return newLinkedHashSet(
				ontologyService.getAllSemanticTypes().stream().map(SemanticType::getName).collect(toList()));
	}

	private List<BiobankUniverse> getUserBiobankUniverses()
	{
		return biobankUniverseService.getBiobankUniverses().stream()
				.filter(universe -> SecurityUtils.currentUserIsSu() || universe.getOwner().getUsername()
						.equals(SecurityUtils.getCurrentUsername())).collect(toList());
	}

	private List<BiobankSampleCollection> getBiobankSampleCollecitons()
	{
		return biobankUniverseService.getAllBiobankSampleCollections();
	}

	private String getMappingServiceMenuUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(ID);
	}
}
