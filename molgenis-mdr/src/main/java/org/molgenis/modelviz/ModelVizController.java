package org.molgenis.modelviz;

import static org.molgenis.modelviz.ModelVizController.URI;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping(URI)
public class ModelVizController extends MolgenisPluginController
{
	Logger logger = Logger.getLogger(ModelVizController.class);
	public static final String ID = "modelviz";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-modelviz";
	private static final String MAIN_VIEW_NAME = "view-modeldata";
	private final DataService dataService;

	@Autowired
	public ModelVizController(DataService dataService)
	{
		super(URI);
		logger.info(">>>>  URI: "+URI) ;
		this.dataService = dataService;
	}

	@RequestMapping
	public String showData(Model model)
	{
		return MAIN_VIEW_NAME;
	}

	@RequestMapping(value="/viewer")
	public String showView(@RequestParam(value = "identifier", required = false) String entityId, @RequestParam(value = "neighbors", required = false, defaultValue="2") int neighbors, Model model)
	{
		model.addAttribute("entityIdentifier", entityId);
		model.addAttribute("neighbors", neighbors);
		return VIEW_NAME;
	}

	@RequestMapping(value="/entity", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object mirrorRestX(@RequestParam(value = "identifier", required = false) String entityId, 
			HttpMethod method) throws URISyntaxException
	{
		logger.info(">>>>>> Query: "+entityId+" Method: "+method) ;
	    java.net.URI uri = new java.net.URI("http", null, "localhost", 9000, "/entity", "identifier="+entityId, null);
		logger.info(">>>>>> Query: "+uri.toString()) ;
	    RestTemplate restTemplate = new RestTemplate();
	    ResponseEntity<Object> responseEntity =
	        restTemplate.exchange(uri, method, new HttpEntity<Object>(""), Object.class);
		//logger.info(">>>>>> Response body: "+responseEntity.getBody()) ;
	    return responseEntity;
	}

}
