package org.molgenis.modelviz.model;

public class ModelAttribute {


	private String identifier;
	
	private String name;
	
	private String description;
	
	/* type:
	 * attribute, associationEnd
	 */
	private String type;
	
	/*
	 * typeQualifier:
	 * not used
	 */
	private String typeQualifier;

	/*
	 * typeName:
	 * Name of the Entity which classifies the attribute
	 */
	private String typeName;
	
	/*
	 * typeIdentifier:
	 * Identifier of the Entity which classifies the attribute
	 */
	private String typeIdentifier;
	
	/*
	 * navigable:
	 * Is attribute navigable to the classifier
	 */
	
	/*
	 * Type of type entity  
	 */
	private String typeType ;
	
	private boolean navigable;
	
	/*aggregation:
	 * Not used currently. 
	 * aggregation, composition
	 */
	private String aggregation; 
	
	private String lowerBound;
	
	private String upperBound;

	/*
	 * typeName:
	 * Name of the Association Entity which classifies the association itself (in case the attribute is associationEnd)
	 */
	private String associationName;
	
	/*
	 * associationIdentifier:
	 * Identifier of the Association Entity which classifies the association itself (in case the attribute is associationEnd)
	 */
	private String associationIdentifier;

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

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeIdentifier() {
		return typeIdentifier;
	}

	public void setTypeIdentifier(String typeIdentifier) {
		this.typeIdentifier = typeIdentifier;
	}

	public boolean isNavigable() {
		return navigable;
	}

	public void setNavigable(boolean navigable) {
		this.navigable = navigable;
	}

	public String getAggregation() {
		return aggregation;
	}

	public void setAggregation(String aggregation) {
		this.aggregation = aggregation;
	}

	public String getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(String lowerBound) {
		this.lowerBound = lowerBound;
	}

	public String getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(String upperBound) {
		this.upperBound = upperBound;
	}

	public String getAssociationName() {
		return associationName;
	}

	public void setAssociationName(String associationName) {
		this.associationName = associationName;
	}

	public String getAssociationIdentifier() {
		return associationIdentifier;
	}

	public void setAssociationIdentifier(String associationIdentifier) {
		this.associationIdentifier = associationIdentifier;
	}

	public String getTypeType() {
		return typeType;
	}

	public void setTypeType(String typeType) {
		this.typeType = typeType;
	}

	
}
