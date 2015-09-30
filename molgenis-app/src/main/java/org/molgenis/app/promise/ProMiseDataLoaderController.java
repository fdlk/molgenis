package org.molgenis.app.promise;

import static org.molgenis.app.promise.ProMiseDataLoaderController.URI;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class ProMiseDataLoaderController extends MolgenisPluginController
{
	public static final String ID = "promiseloader";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final ProMiseDataParser promiseDataParser;
	private final DataService dataService;
	private final RadboudMapper radboudMapper;

	@Autowired
	public ProMiseDataLoaderController(ProMiseDataParser proMiseDataParser, DataService dataService,
			RadboudMapper radboudMapper)
	{
		super(URI);
		this.promiseDataParser = Objects.requireNonNull(proMiseDataParser);
		this.dataService = Objects.requireNonNull(dataService);
		this.radboudMapper = Objects.requireNonNull(radboudMapper);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-promiseloader";
	}

	@RequestMapping(value = "map/{id}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public void map(@PathVariable("id") String biobankId) throws IOException
	{
		switch (biobankId)
		{
			case "RADBOUD":
				radboudMapper.map(biobankId);
				break;
			case "IBD":
				break;
			default:
				break;
		}
	}

	@RequestMapping(value = "load", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public void load() throws IOException
	{
		Map<Integer, String> seqNrMap = new LinkedHashMap<Integer, String>();
		seqNrMap.put(0, "Biobanks");
		// seqNrMap.put(1, "Samples");
		seqNrMap.put(10, "Queries");
		seqNrMap.put(11, "Tables");
		seqNrMap.put(12, "Items");
		seqNrMap.put(13, "Headers");
		seqNrMap.put(14, "Labels");
		seqNrMap.put(15, "Centers");
		seqNrMap.put(16, "Users");
		seqNrMap.put(17, "Logins");
		seqNrMap.put(18, "Contact");

		for (Map.Entry<Integer, String> entry : seqNrMap.entrySet())
		{
			System.out.println("#### loading " + entry.getKey());
			// load(entry.getKey(), entry.getValue());
		}
	}

	private void load(String biobankId, Integer seqNr, String label) throws IOException
	{
		Iterable<Entity> entities = promiseDataParser.parse(biobankId, seqNr);

		String promiseEntityName = "promise" + '_' + label.toLowerCase();
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(promiseEntityName);
		entityMetaData.setLabel("ProMISe " + label);
		entityMetaData.addAttribute("_id").setIdAttribute(true).setAuto(true).setVisible(false).setNillable(false);

		Set<String> attrNames = new HashSet<String>();
		for (Entity entity : entities)
		{
			for (String attrName : entity.getAttributeNames())
			{
				if (!attrNames.contains(attrName))
				{
					entityMetaData.addAttribute(attrName).setDataType(MolgenisFieldTypes.TEXT).setNillable(true);
					attrNames.add(attrName);
				}
			}
		}

		if (dataService.getMeta().getEntityMetaData(promiseEntityName) != null)
		{
			dataService.getMeta().deleteEntityMeta(promiseEntityName);
		}
		dataService.getMeta().addEntityMeta(entityMetaData);
		dataService.add(promiseEntityName, entities);
	}
}
