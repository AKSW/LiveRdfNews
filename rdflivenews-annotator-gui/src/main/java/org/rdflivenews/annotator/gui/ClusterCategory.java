package org.rdflivenews.annotator.gui;

public enum ClusterCategory {
	
	UNKNOWN("unknown"),
	UNIVERSITY("university"),
	CREATOR("creator"),
	PERSONAL("personal"),
	SPORT("sport"),
	ORGANIZATION("organization"),
	PLACE("place"),
	MEMBER("member");
	
	private String name;
	
	private ClusterCategory(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
