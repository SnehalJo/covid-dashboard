package com.covid.dashboard.model;

import lombok.Data;

import java.util.Map;

@Data
public class Cases {

	private Map<Key, Case> casesByCountryCity;
	
	private Map<String, Case> casesByCountry;

	private Map<String,Map<String, Case>> casesByCountryRegionAndDate;
	private Map<String,Map<String, Case>> casesByCountryAndDate;
	
}
