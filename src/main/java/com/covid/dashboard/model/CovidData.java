package com.covid.dashboard.model;

import lombok.Data;

@Data
public class CovidData {

    private Case covidCase;
    boolean containmentZone;
    boolean workFromOffice;

}
