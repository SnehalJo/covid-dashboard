package com.covid.dashboard.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Case {

	@JsonProperty(value = "FIPS")
	private String fips;
	@JsonProperty(value = "Admin2")
    private String admin2;
	@JsonProperty(value = "Province_State")
    private String provinceState;
	@JsonProperty(value = "Country_Region")
    private String countryRegion;
	@JsonProperty(value = "Last_Update")
    private String lastUpdate;
	@JsonProperty(value = "Lat")
    private String lat;
	@JsonProperty(value = "Long_")
    private String longitude;
	@JsonProperty(value = "Confirmed")
    private long confirmed;
	@JsonProperty(value = "Deaths")
    private long deaths;
	@JsonProperty(value = "Recovered")
    private long recovered;
	@JsonProperty(value = "Active")
    private long active;
	@JsonProperty(value = "Combined_Key")
    private String combinedKey;
	@JsonProperty(value = "Incident_Rate")
    private float incidentRate;
	@JsonProperty(value = "Case_Fatality_Ratio")
    private float fatalityRatio;

}
