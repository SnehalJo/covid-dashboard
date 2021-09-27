package com.covid.dashboard.model;

import lombok.Data;

import java.util.List;

@Data
public class CovidResponse {

	private Case  cases;
	private List<CovidData> caseList;
	private CovidData covidData;

}
