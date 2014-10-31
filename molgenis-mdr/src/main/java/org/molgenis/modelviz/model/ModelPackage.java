package org.molgenis.modelviz.model;

public class ModelPackage {

	private String identifier;
	private String name;
	private String description;
	private String nameSpace;
	private int numberOfEntities;
	
	
	public ModelPackage(String identifier, String name, String description,
			String nameSpace, int entities) {
		super();
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.nameSpace = nameSpace;
		this.numberOfEntities = entities;
	}
	
	 
	public ModelPackage() {
		super();
	}

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
	public String getNameSpace() {
		return nameSpace;
	}
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}
	public int getNumberOfEntities() {
		return numberOfEntities;
	}
	public void setNumberOfEntities(int entities) {
		this.numberOfEntities = entities;
	}
	
	
}
