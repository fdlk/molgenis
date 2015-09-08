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
	private static final String EXPRESSION_DATA = "expression_data";
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

	@RequestMapping(value = "/deconvolution")
	public String deconvolutionPlots()
	{
		return "view-deconvolution";
	}

	@RequestMapping(value = "/validate", method = POST)
	@ResponseBody
	public String validateGeneInput(@RequestBody String inputValue)
	{

		// Entity expData = dataService.findOne(EXPRESSION_DATA, new QueryImpl().eq("GeneNames", inputValue));

		// ArrayList<String> celltypes = new ArrayList<String>();
		// HashMap<String,List<String>> expressionValues = new HashMap<String,List<String>>();
		// ArrayList<String> validatedInput = new ArrayList<String>();
		// HashMap<String,String> validatedInput = new HashMap<String,String>();
		String validatedInput = new String();

		// if (expData != null){
		String[] genes = inputValue.toString().split(",");

		for (String gene : genes)
		{
			Entity validatedGene = dataService.findOne(EXPRESSION_DATA,
					new QueryImpl().search("GeneNames", gene.replaceAll("\\s", "")));
			if (validatedGene != null)
			{
				System.out.println("SUCCES: " + gene);
				validatedInput += "Success:" + validatedGene.toString() + ",";
			}
			else
			{
				System.out.println("FAIL: " + gene);
				validatedInput += "Fail:" + gene + ",";
			}

		}
		return validatedInput.substring(0, validatedInput.length() - 1);
	}

}

// for(String attribute:expData.getAttributeNames()){
//
// celltypes.add(attribute + ":" + expData.get(attribute));
//
//
// }
// expressionValues.put(expData.toString(),celltypes);
// System.out.println(genes);

