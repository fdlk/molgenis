package org.molgenis.modelviz.model;

public class AdminInfo {

	private String nameSpace;
	private String model ;
	private String stewardingOrganization;
	
	public AdminInfo(String nameSpace, String model,
			String stewardingOrganization) {
		super();
		this.nameSpace = nameSpace;
		this.model = model;
		this.stewardingOrganization = stewardingOrganization;
	}
	public String getNameSpace() {
		return nameSpace;
	}
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getStewardingOrganization() {
		return stewardingOrganization;
	}
	public void setStewardingOrganization(String stewardingOrganization) {
		this.stewardingOrganization = stewardingOrganization;
	}
	
}
