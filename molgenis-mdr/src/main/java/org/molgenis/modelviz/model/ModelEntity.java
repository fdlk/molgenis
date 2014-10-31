package org.molgenis.modelviz.model;

import java.util.ArrayList;
import java.util.List;




public class ModelEntity {

	private String identifier;
	
	private String name;
	
	private String description;
	
	/* type:
	 * interface, class, association, primitive, enumeration
	 */
	private String type;
	
	/*
	 * typeQualifier:
	 * abstract, concrete
	 */
	private String typeQualifier;
	
	/*
	 * Level of relationships has. E.g. A has level 4: A->B->C->D 
	 */
	private int neighbourLevel;
	
	private List<ModelEntity> relatedEntities = new ArrayList<ModelEntity>() ;
	
	private List<String> parentIDs = new ArrayList<String>();

	private String packageIdentifier;
	
	private String packageName;
	
	private String packageDescription;

	private int undefs = -1; //Number of undefined attributes
	
	private List<ModelAttribute> attributes = new ArrayList<ModelAttribute>();
		
	private int numberOfAttributes ;
	
	public ModelEntity(String identifier, String name, String description,
			String type, String typeQualifier, int neighbourLevel,
			List<ModelEntity> relatedEntities, List<String> parentIDs,
			String packageIdentifier, String packageName,
			String packageDescription, int undefs,
			List<ModelAttribute> attributes, int numberOfAttributes) {
		super();
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.type = type;
		this.typeQualifier = typeQualifier;
		this.neighbourLevel = neighbourLevel;
		this.relatedEntities = relatedEntities;
		this.parentIDs = parentIDs;
		this.packageIdentifier = packageIdentifier;
		this.packageName = packageName;
		this.packageDescription = packageDescription;
		this.undefs = undefs;
		this.attributes = attributes;
		this.numberOfAttributes = numberOfAttributes;
	}
	
	public ModelEntity() {
		super();
	}
	/*
	 * Getters/Setters
	 */
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeQualifier() {
		return typeQualifier;
	}

	public void setTypeQualifier(String typeQualifier) {
		this.typeQualifier = typeQualifier;
	}

	public int getNeighbourLevel() {
		return neighbourLevel;
	}

	public void setNeighbourLevel(int neighbourLevel) {
		this.neighbourLevel = neighbourLevel;
	}

	public String getPackageIdentifier() {
		return packageIdentifier;
	}

	public void setPackageIdentifier(String packageIdentifier) {
		this.packageIdentifier = packageIdentifier;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageDescription() {
		return packageDescription;
	}

	public void setPackageDescription(String packageDescription) {
		this.packageDescription = packageDescription;
	}

	public List<ModelEntity> getRelatedEntities() {
		return relatedEntities;
	}

	public void setRelatedEntities(List<ModelEntity> entities) {
		this.relatedEntities = entities;
	}


	public List<ModelAttribute> getAttributes() {
		return attributes;
	}



	public void setAttributes(List<ModelAttribute> attributes) {
		this.attributes = attributes;
	}

	public List<String> getParentIDs() {
		return parentIDs;
	}

	public void setParentIDs(List<String> parentIDs) {
		this.parentIDs = parentIDs;
	}

	public int getUndefs() {
		return undefs;
	}

	public void setUndefs(int undefs) {
		this.undefs = undefs;
	}

	public int getNumberOfAttributes() {
		return numberOfAttributes;
	}

	public void setNumberOfAttributes(int numberOfAttributes) {
		this.numberOfAttributes = numberOfAttributes;
	}


}
