package org.molgenis.lncrna.explorer.controller;

import static org.molgenis.lncrna.explorer.controller.LncrnaExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping(URI)
public class LncrnaExplorerController extends MolgenisPluginController
{
	public static final String ID = "lncrna";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	
	
	@Autowired
	private DataService dataService;
	
	  
	public LncrnaExplorerController()
	{
		super(URI);
	}

	@RequestMapping
	public String init(Model model)
	{
		return "view-lncrnaexplorer";
	}
	
	
	
	@RequestMapping(value = "/validate", method = POST)
	@ResponseBody
	public void validateGeneInput(@RequestBody String inputValue){
		
		Entity expData = dataService.findOne("expression_data", new QueryImpl().eq("GeneNames", inputValue));
		
		System.out.println(expData);
		System.out.println(dataService.getRepository("expression_data").findOne(inputValue));
		
//		if (dataService.findOne("expression_data", new QueryImpl().eq("GeneNames", inputValue)) != null){
//			
//
//			return "Success "; 
//		} else{
//			return "Oops";
//		}
	}

}
