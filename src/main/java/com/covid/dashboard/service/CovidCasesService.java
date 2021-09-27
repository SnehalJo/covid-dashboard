package com.covid.dashboard.service;

import java.text.SimpleDateFormat;
import java.util.*;

import com.covid.dashboard.constants.CovidDashBoardConstants;
import com.covid.dashboard.model.*;
import com.covid.dashboard.helper.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CovidCasesService {

    @Autowired
    private Cases cases;

    public CovidResponse getCaseCountry(Map<?, ?> queryData) {
		log.debug("Inside getCaseCountry");
        String country = (String) queryData.get(CovidDashBoardConstants.COUNTRY);
		log.info(country);
        CovidResponse resp = new CovidResponse();
        resp.setCases(cases.getCasesByCountry().get(country));
        return resp;

    }

    public CovidResponse getCaseCountryCity(Map<?, ?> queryData) {
		log.info("Inside getCaseCountryCity");
        String country = (String) queryData.get(CovidDashBoardConstants.COUNTRY);
        String state = (String) queryData.get(CovidDashBoardConstants.STATE);
        log.info("Country"+country);
		log.info("State"+state);
        Key key = new Key();
        key.setCountryRegion(country);
        key.setProvinceState(state);
        CovidResponse resp = new CovidResponse();
        resp.setCases(cases.getCasesByCountryCity().get(key));
        return resp;
    }

    public CovidResponse getCovidDataByFilters(Map<?, ?> queryData) {
    	log.debug("Inside getCovidDataByFilters");
        Map<String, String> filters = new HashMap();
        String frequency;
        String fromDate;
        String toDate;

        frequency = (String) queryData.get(CovidDashBoardConstants.FREQUENCY);
        if (frequency != null) {
            filters.put(CovidDashBoardConstants.FREQUENCY, frequency);
            calculateFromDateToDateForSelectedRange(frequency, filters);
        } else {
            fromDate = (String) queryData.get(CovidDashBoardConstants.FROM_DATE);
            toDate = (String) queryData.get(CovidDashBoardConstants.TO_DATE);
            filters.put(CovidDashBoardConstants.FROM_DATE, fromDate);
            filters.put(CovidDashBoardConstants.TO_DATE, toDate);
        }
        String country = (String) queryData.get(CovidDashBoardConstants.COUNTRY);
        if (country != null) {
            filters.put(CovidDashBoardConstants.COUNTRY, country);
        }

        String state = (String) queryData.get(CovidDashBoardConstants.STATE);
        if (state != null) {
            filters.put(CovidDashBoardConstants.STATE, state);
        }

        Map<String, Case> casesByCountryCity = new HashMap<>();
        Map<String, Case> casesByCountry = new HashMap<>();
        populateCovidDataForSelectedFilters(filters, casesByCountryCity, casesByCountry);
        CovidResponse resp = new CovidResponse();
        CovidData covidData = new CovidData();
        String latitude = (String) queryData.get(CovidDashBoardConstants.LATITUDE);
        String longitude = (String) queryData.get(CovidDashBoardConstants.LONGITUDE);
        Integer totalPopulation = (Integer) queryData.get(CovidDashBoardConstants.TOTAL_POPULATION);
        Double threshold = (Double) queryData.get(CovidDashBoardConstants.THRESHOLD);

        if (filters.containsKey(CovidDashBoardConstants.STATE)) {
            covidData.setCovidCase(casesByCountryCity.get(filters.get(CovidDashBoardConstants.STATE)));
            if (casesByCountryCity.get(filters.get(CovidDashBoardConstants.STATE)).getActive() + casesByCountryCity.get(filters.get(CovidDashBoardConstants.STATE)).getConfirmed() > casesByCountryCity.get(filters.get(CovidDashBoardConstants.STATE)).getRecovered()) {
                covidData.setContainmentZone(true);
            }
            resp.setCovidData(covidData);
        } else if (filters.containsKey(CovidDashBoardConstants.COUNTRY)) {
            covidData.setCovidCase(casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY)));
			covidData.setContainmentZone(casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY)).getActive() + casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY)).getConfirmed() > casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY)).getRecovered());
            if (latitude != null && longitude != null && totalPopulation != 0 && threshold != 0) {
                if (casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY)).getLat().equals(latitude) && casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY)).getLongitude().equals(longitude)) {
                    float activePercentage = (casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY)).getActive() * 100) / totalPopulation;
                    if (activePercentage < threshold) {
                        covidData.setWorkFromOffice(true);
                    } else {
                        covidData.setWorkFromOffice(false);
                    }
                }
            }
            resp.setCovidData(covidData);
        } else {

            List<CovidData> cases = new ArrayList<>();
            for (Map.Entry<String, Case> caseEntry : casesByCountry.entrySet()) {
                CovidData data = new CovidData();
                if (latitude != null && longitude != null && totalPopulation != 0 && threshold != 0) {
                    if (caseEntry.getValue().getLat().equals(latitude) && caseEntry.getValue().getLongitude().equals(longitude)) {
                        float activePercentage = (caseEntry.getValue().getActive() * 100) / totalPopulation;
                        if (activePercentage < threshold) {
                            data.setWorkFromOffice(true);
                        } else {
                            data.setWorkFromOffice(false);
                        }
                    }
                } else {
                    data.setCovidCase(caseEntry.getValue());

                    if (caseEntry.getValue().getActive() + caseEntry.getValue().getConfirmed() > caseEntry.getValue().getRecovered()) {
                        covidData.setContainmentZone(true);
                    } else {
                        covidData.setContainmentZone(false);
                    }
                }
                cases.add(data);
                resp.setCaseList(cases);
            }
        }
        return resp;
    }

    private void populateCovidDataForSelectedFilters(Map<String, String> filters, Map<String, Case> casesByCountryCity, Map<String, Case> casesByCountry) {
		log.debug("Inside populateCovidDataForSelectedFilters method");
        Map<String, Map<String, Case>> casesByCountryRegionAndDate = cases.getCasesByCountryRegionAndDate();
        Map<String, Map<String, Case>> casesByCountryAndDate = cases.getCasesByCountryAndDate();
        List<String> keys = getListOfKeys(filters);

        for (String csvFileName : keys) {

            if (filters.get(CovidDashBoardConstants.STATE) != null) {
                if (casesByCountryRegionAndDate.containsKey(csvFileName)) {
                    if (casesByCountryRegionAndDate.get(csvFileName).containsKey(filters.get(CovidDashBoardConstants.STATE))) {
                        if (!casesByCountryCity.isEmpty() && casesByCountryCity.containsKey(filters.get(CovidDashBoardConstants.STATE))) {
                            Case caseData = casesByCountryCity.get(filters.get(CovidDashBoardConstants.STATE));
                            caseData.setDeaths(caseData.getDeaths() + casesByCountryRegionAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.STATE)).getDeaths());
                            caseData.setConfirmed(caseData.getConfirmed() + casesByCountryRegionAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.STATE)).getConfirmed());
                            caseData.setRecovered(caseData.getRecovered() + casesByCountryRegionAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.STATE)).getRecovered());
                            caseData.setActive(caseData.getActive() + casesByCountryRegionAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.STATE)).getActive());
                            caseData.setIncidentRate(caseData.getIncidentRate() + casesByCountryRegionAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.STATE)).getIncidentRate());
                            casesByCountryCity.put(filters.get(CovidDashBoardConstants.STATE), caseData);
                        } else {
                            casesByCountryCity.put(filters.get(CovidDashBoardConstants.STATE), casesByCountryRegionAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.STATE)));
                        }
                    }
                }

            } else if (filters.get(CovidDashBoardConstants.COUNTRY) != null) {
                if (casesByCountryAndDate.containsKey(csvFileName)) {
                    if (casesByCountryAndDate.get(csvFileName).containsKey(filters.get(CovidDashBoardConstants.COUNTRY))) {
                        if (!casesByCountry.isEmpty() && casesByCountry.containsKey(filters.get(CovidDashBoardConstants.COUNTRY))) {
                            Case caseData = casesByCountry.get(filters.get(CovidDashBoardConstants.COUNTRY));
                            caseData.setDeaths(caseData.getDeaths() + casesByCountryAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.COUNTRY)).getDeaths());
                            caseData.setConfirmed(caseData.getConfirmed() + casesByCountryAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.COUNTRY)).getConfirmed());
                            caseData.setRecovered(caseData.getRecovered() + casesByCountryAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.COUNTRY)).getRecovered());
                            caseData.setActive(caseData.getActive() + casesByCountryAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.COUNTRY)).getActive());
                            caseData.setIncidentRate(caseData.getIncidentRate() + casesByCountryAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.COUNTRY)).getIncidentRate());
                            casesByCountry.put(filters.get(CovidDashBoardConstants.COUNTRY), caseData);
                        } else {
                            casesByCountry.put(filters.get(CovidDashBoardConstants.COUNTRY), casesByCountryAndDate.get(csvFileName).get(filters.get(CovidDashBoardConstants.COUNTRY)));
                        }
                    }
                }
            } else {
                if (casesByCountryAndDate.containsKey(csvFileName)) {
                    for (Map.Entry<String, Case> caseEntry : casesByCountryAndDate.get(csvFileName).entrySet()) {
                        if (casesByCountryAndDate.get(csvFileName).containsKey(caseEntry.getKey())) {
                            if (!casesByCountry.isEmpty() && casesByCountry.containsKey(caseEntry.getKey())) {
                                Case caseData = casesByCountry.get(caseEntry.getKey());
                                caseData.setDeaths(caseData.getDeaths() + casesByCountryAndDate.get(csvFileName).get(caseEntry.getKey()).getDeaths());
                                caseData.setConfirmed(caseData.getConfirmed() + casesByCountryAndDate.get(csvFileName).get(caseEntry.getKey()).getConfirmed());
                                caseData.setRecovered(caseData.getRecovered() + casesByCountryAndDate.get(csvFileName).get(caseEntry.getKey()).getRecovered());
                                caseData.setActive(caseData.getActive() + casesByCountryAndDate.get(csvFileName).get(caseEntry.getKey()).getActive());
                                caseData.setIncidentRate(caseData.getIncidentRate() + casesByCountryAndDate.get(csvFileName).get(caseEntry.getKey()).getIncidentRate());
                                casesByCountry.put(caseEntry.getKey(), caseData);
                            } else {
                                casesByCountry.put(caseEntry.getKey(), casesByCountryAndDate.get(csvFileName).get(caseEntry.getKey()));
                            }
                        }
                    }

                }
            }
        }


    }

    private static List<String> getListOfKeys(Map<String, String> filters) {
    	log.info("Inside getListOfKeys method");
        ArrayList<String> datesList = new ArrayList<>();
        Date startDate = DateUtils.getDateFormatted(filters.get(CovidDashBoardConstants.FROM_DATE), "mm-dd-yyyy");
        Date endDate = DateUtils.getDateFormatted(filters.get(CovidDashBoardConstants.TO_DATE), "mm-dd-yyyy");
        String suffix = ".csv";
        String convertedDate;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);

        while (cal1.before(cal2) || cal1.equals(cal2)) {
            convertedDate = DateUtils.getDateFormatted(cal1.getTime(), "mm-dd-yyyy");
            datesList.add(convertedDate + suffix);
            cal1.add(Calendar.DATE, 1);
        }
        return datesList;
    }

    /***
     * calculateFromDateToDateForSelectedRange is used to get from date and to date for each type of
     * Filter type apply from Frontend like Month, Week, Day.
     */
    private void calculateFromDateToDateForSelectedRange(String frequency, Map<String, String> filters) {

        String toDate;
        String fromDate;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        Calendar cal = Calendar.getInstance();
        toDate = sdf.format(cal.getTime());

        if (frequency.equals("Weekly")) {
            cal.add(Calendar.DATE, -7);
        } else if (frequency.equals("Monthly")) {
            cal.add(Calendar.MONTH, -1);
        } else if (frequency.equals("Daily")) {
            cal.add(Calendar.DATE, -1);
        }
        fromDate = sdf.format(cal.getTime());
        filters.put(CovidDashBoardConstants.FROM_DATE, fromDate);
        filters.put(CovidDashBoardConstants.TO_DATE, toDate);
    }


}
