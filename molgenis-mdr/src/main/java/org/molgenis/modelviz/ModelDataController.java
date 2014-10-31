package org.molgenis.modelviz;

import static org.molgenis.modelviz.ModelDataController.URI;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.modelviz.model.AdminInfo;
import org.molgenis.modelviz.model.ModelAttribute;
import org.molgenis.modelviz.model.ModelEntity;
import org.molgenis.modelviz.model.ModelPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class ModelDataController extends MolgenisPluginController
{

	Logger logger = Logger.getLogger(ModelDataController.class);
	public static final String ID = "modeldata";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final MetaDataService metaDataService;

	@Autowired
	public ModelDataController(MetaDataService metaDataService)
	{
		super(URI);
		this.metaDataService = metaDataService;
	}

	/*
	 * Data management queries: select * from MDRAttribute a where not exists ( select 1 from MDREntity e where
	 * a.classifierIdentifier = e.identifier )
	 */

	@RequestMapping(value = "/administratedModels", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object getAdminInfo()
	{
		List<AdminInfo> adminInfo = new ArrayList<AdminInfo>();
		// empty list for now
		return adminInfo;
	}

	// query entities based on either name space ids or pkg ids
	@RequestMapping(value = "/entities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object getEntities(
			@RequestParam(value = "packageIdentifier", required = false) String pkgId[],
			@RequestParam(value = "namespaceIdentifier", required = false) String nsId[])
	{
		List<ModelEntity> entities = new ArrayList<ModelEntity>();

		if (pkgId != null)
		{
			for (int j = 0; j < pkgId.length; j++)
			{
				String packageId = pkgId[j];
				Package pack = metaDataService.getPackage(packageId);
				for (EntityMetaData emd : pack.getEntityMetaDatas())
				{
					ModelEntity e = new ModelEntity();
					e.setIdentifier(emd.getName());
					e.setName(emd.getSimpleName());
					e.setType("entity");
					e.setPackageIdentifier(packageId);
					e.setPackageDescription(pack.getDescription());
					e.setPackageName(pack.getSimpleName());
					e.setTypeQualifier("");
					e.setNeighbourLevel(-1);
					e.setUndefs(0);
					entities.add(e);
				}
			}
		}
		return entities;
	}

	@RequestMapping(value = "/packages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object getPackages(
			@RequestParam(value = "namespaceIdentifier", required = true) String nsId[], HttpMethod method)
	{
		logger.info("Query packages. Number of params= " + nsId.length);

		List<ModelPackage> pkgs = new ArrayList<ModelPackage>();

		for (int i = 0; i < nsId.length; i++)
		{
			String namespaceId = nsId[i];
			logger.info("Query attributes: " + namespaceId);
			org.molgenis.data.Package pack = metaDataService.getPackage(namespaceId);
			pkgs.addAll(getModelPackages(namespaceId, pack));
		}
		return pkgs;
	}

	/**
	 * Creates {@link ModelPackage}s for all the packages in this package.
	 * 
	 * @param namespaceId
	 *            the namespace for the {@link ModelPackage}s
	 * @param p
	 *            the {@link Package} to create {@link ModelPackage}s for
	 */
	private List<ModelPackage> getModelPackages(String namespaceId, org.molgenis.data.Package p)
	{
		List<ModelPackage> pkgs = new ArrayList<ModelPackage>();
		ModelPackage mp = new ModelPackage();
		mp.setDescription(p.getDescription());
		mp.setName(p.getSimpleName());
		mp.setIdentifier(p.getName());
		mp.setNameSpace(namespaceId);
		mp.setNumberOfEntities(Iterables.size(p.getEntityMetaDatas()));
		pkgs.add(mp);
		for (org.molgenis.data.Package sp : p.getSubPackages())
		{
			pkgs.addAll(getModelPackages(namespaceId, sp));
		}
		return pkgs;
	}

	@RequestMapping(value = "/attributes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object getEntityAttributes(
			@RequestParam(value = "entityIdentifier", required = true) String entityId[], HttpMethod method)
			throws URISyntaxException
	{

		List<ModelAttribute> attrs = new ArrayList<ModelAttribute>();

		for (int i = 0; i < entityId.length; i++)
		{
			String entityName = entityId[i];
			logger.info("Query attributes: " + entityName);
			EntityMetaData emd = metaDataService.getEntityMetaData(entityName);
			attrs.addAll(getAttributes(emd));
		}
		return attrs;
	}

	/*
	 * Attributes whose type is undefined. Happens if type info is not given in UML or if type/primitives package is not
	 * included as part of the model.
	 */
	@RequestMapping(value = "/entity/incomplete_attributes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object getIncompleteAttributes(
			@RequestParam(value = "namespaceIdentifier", required = true) String namespaceId,
			@RequestParam(value = "unknownType", defaultValue = "true") boolean type, HttpMethod method)
			throws URISyntaxException
	{

		List<HashMap<String, String>> entities = new ArrayList<HashMap<String, String>>();
		// while (false)
		// {
		// HashMap<String, String> e = new HashMap<String, String>();
		// e.put("entityName", "e");
		// e.put("attributeName", "a");
		// e.put("attributeType", "a");
		// e.put("packageName", "p");
		// entities.add(e);
		// }

		return entities;
	}

	@RequestMapping(value = "/entity", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object getEntity(@RequestParam(value = "identifier", required = true) String entityId,
			@RequestParam(value = "neighbours", defaultValue = "1") int neighbours, HttpMethod method)
			throws URISyntaxException
	{
		ModelEntity responseEntity = new ModelEntity();
		logger.debug("Called: " + entityId);

		List<String> id = new ArrayList<String>();
		id.add(entityId);
		List<ModelEntity> e = getEntities(id, neighbours);
		if (e.size() > 0)
		{
			responseEntity = e.get(0);
			if (e.size() > 1)
			{
				logger.warn("More than one entities returned for id " + entityId);
			}
		}

		return responseEntity;
	}

	protected List<ModelAttribute> getAttributes(EntityMetaData emd)
	{
		List<ModelAttribute> attributes = new ArrayList<ModelAttribute>();
		for (AttributeMetaData amd : emd.getAttributes())
		{
			ModelAttribute a = new ModelAttribute();
			a.setIdentifier(emd.getName() + "." + amd.getName()); // FK: made up identifier
			a.setName(amd.getName());
			a.setDescription(amd.getDescription());
			FieldType dataType = amd.getDataType();
			a.setType(dataType.toString()); // renamed to classifier
			a.setTypeName(dataType.toString());
			a.setTypeIdentifier(dataType.toString());
			a.setTypeType(dataType.toString()); // todo: rename
			if (dataType instanceof XrefField)
			{
				a.setNavigable(true);
				a.setAggregation("false");
				a.setAssociationIdentifier("");
				a.setLowerBound(amd.isNillable() ? "0" : "1");
				a.setUpperBound("1");
				a.setAssociationName("");
				a.setTypeQualifier("");
			}
			else if (dataType instanceof MrefField)
			{
				a.setNavigable(true);
				a.setAggregation("false");
				a.setAssociationIdentifier("");
				a.setLowerBound(amd.isNillable() ? "0" : "1");
				a.setUpperBound("*");
				a.setAssociationName("");
				a.setTypeQualifier("");
			}
			attributes.add(a);
		}
		return attributes;
	}

	protected List<ModelEntity> getEntities(List<String> ids, int neighbours)
	{
		List<ModelEntity> entities = new ArrayList<ModelEntity>();
		for (String id : ids)
		{
			System.out.println(id);
			EntityMetaData emd = metaDataService.getEntityMetaData(id);
			if (emd != null)
			{
				ModelEntity e = new ModelEntity();
				List<ModelAttribute> attributes = getAttributes(emd);
				e.setAttributes(attributes);
				e.setNumberOfAttributes(attributes.size());
				e.setDescription(e.getDescription());
				e.setIdentifier(id);
				e.setName(emd.getName());
				e.setPackageDescription(emd.getPackage().getDescription());
				e.setPackageIdentifier(emd.getPackage().getName());
				e.setPackageName(emd.getPackage().getSimpleName());
				e.setNeighbourLevel(1);
				e.setParentIDs(Collections.<String> emptyList());
				e.setRelatedEntities(Collections.<ModelEntity> emptyList());
				e.setType("Entity");
				e.setUndefs(0);
				e.setTypeQualifier("default");
				entities.add(e);
			}
			else
			{
				logger.warn("No entity found for id" + id);
			}
		}

		return entities;
	}
}
