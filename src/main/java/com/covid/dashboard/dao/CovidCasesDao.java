package com.covid.dashboard.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.covid.dashboard.model.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Repository
public class CovidCasesDao {

    @Value("${gitrepo}")
    private String gitRepo;

    @Value("${gitrepotreeurl}")
    private String gitRepoTreeUrl;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RedissonClient redissonClient;

    public List<GitTree> getFileNameList() {
        ResponseEntity<GitRepoTreeModel> result = null;
        result = restTemplate.exchange(gitRepoTreeUrl, HttpMethod.GET, null, GitRepoTreeModel.class);
        log.info("result" + result);
        return result.getBody().getTree();

    }

    public Cases getData(Date date) {
        List<GitTree> gitTreeList = getFileNameList();
        Map<Key, Case> allMapsByCountryAndRegion = new HashMap<>();
        Map<String, Case> allMapsByCountry = new HashMap<>();
        Map<String, Map<String, Case>> mapsByCountryAndRegion = new HashMap<>();
        Map<String, Map<String, Case>> countryAndRegionMap = new HashMap<>();
        Map<String, Case> internalMapsByCountryAndRegion = new HashMap<>();
        Cases caseData = new Cases();
        Map<String, Map<String, Case>> mapByCountry = new HashMap<>();
        Map<String, Case> internalMapByCountry = new HashMap<>();
        List<String> csvFileNameList = new ArrayList<>();
        for (GitTree gitTree : gitTreeList
        ) {
            String csvFileName = null;
            if (gitTree.getPath().contains("csse_covid_19_data/csse_covid_19_daily_reports")) {
                csvFileName = gitTree.getPath().replace("csse_covid_19_data/csse_covid_19_daily_reports/", "");
                log.info("csvFileName:", csvFileName);
                if (!csvFileName.isEmpty() && csvFileName.contains(".csv")) {
                    System.out.println(csvFileName);
                    csvFileNameList.add(csvFileName);
                }
            }
        }
        csvFileNameList = csvFileNameList.stream().limit(500).collect(Collectors.toList());
        for (String csvFileName : csvFileNameList) {

            java.net.URL url = null;
            try {
                url = new java.net.URL(gitRepo + csvFileName);
                java.net.URLConnection uc;
                uc = url.openConnection();
                uc.setRequestProperty("X-Requested-With", "Curl");
                BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                CsvSchema casesSchema = CsvSchema.emptySchema().withHeader();
                CsvMapper csvMapper = new CsvMapper();
                MappingIterator<Case> cases = csvMapper.readerFor(Case.class).with(casesSchema).readValues(reader);
                List<Case> map = cases.readAll();

                for (Case covidCase : map) {
                    Key key = new Key();
                    key.setCountryRegion(covidCase.getCountryRegion());
                    key.setProvinceState(covidCase.getProvinceState());
                    //this map is for all data till date country wise
                    if (!allMapsByCountry.isEmpty() && allMapsByCountry.containsKey(covidCase.getCountryRegion())) {
                        Case internalCovidCase = allMapsByCountry.get(covidCase.getCountryRegion());
                        internalCovidCase.setConfirmed(internalCovidCase.getConfirmed() + covidCase.getConfirmed());
                        internalCovidCase.setDeaths(internalCovidCase.getDeaths() + covidCase.getDeaths());
                        internalCovidCase.setActive(internalCovidCase.getConfirmed() + covidCase.getActive());
                        internalCovidCase.setRecovered(internalCovidCase.getRecovered() + covidCase.getRecovered());
                        allMapsByCountry.put(covidCase.getCountryRegion(), internalCovidCase);
                    } else {
                        allMapsByCountry.put(covidCase.getCountryRegion(), covidCase);
                    }
                    //this is overall data for all city wise till date
                    if (covidCase.getProvinceState() != null && !covidCase.getProvinceState().isEmpty()) {
                        if (!allMapsByCountryAndRegion.isEmpty() && allMapsByCountryAndRegion.containsKey(key)) {
                            Case caseData1 = allMapsByCountryAndRegion.get(key);
                            caseData1.setActive(caseData1.getActive() + covidCase.getActive());
                            caseData1.setRecovered(caseData1.getRecovered() + covidCase.getRecovered());
                            caseData1.setConfirmed(caseData1.getConfirmed() + covidCase.getConfirmed());
                            caseData1.setDeaths(caseData1.getDeaths() + covidCase.getDeaths());
                            allMapsByCountryAndRegion.put(key, caseData1);
                        } else {
                            allMapsByCountryAndRegion.put(key, covidCase);
                        }
                        //insert data in map date wise where date is key and value is the map for country and region
                        internalMapsByCountryAndRegion.put(covidCase.getProvinceState(), covidCase);
                        mapsByCountryAndRegion.put(csvFileName, internalMapsByCountryAndRegion);
                    }
                    // insert data in map date wise where date is the key and value is country wise date
                    if (!internalMapByCountry.isEmpty() && internalMapByCountry.containsKey(covidCase.getCountryRegion())) {
                        Case caseData1 = internalMapByCountry.get(covidCase.getCountryRegion());
                        caseData1.setDeaths(caseData1.getDeaths() + covidCase.getDeaths());
                        caseData1.setConfirmed(caseData1.getConfirmed() + covidCase.getConfirmed());
                        caseData1.setRecovered(caseData1.getRecovered() + covidCase.getRecovered());
                        caseData1.setActive(caseData1.getActive() + covidCase.getActive());
                        caseData1.setIncidentRate(caseData1.getIncidentRate() + covidCase.getIncidentRate());
                        internalMapByCountry.put(covidCase.getCountryRegion(), caseData1);
                    } else {
                        internalMapByCountry.put(covidCase.getCountryRegion(), covidCase);
                    }
                    mapByCountry.put(csvFileName, internalMapByCountry);
                }

            } catch (IOException e) {

            }
        }
        caseData.setCasesByCountry(allMapsByCountry);
        caseData.setCasesByCountryRegionAndDate(mapsByCountryAndRegion);
        caseData.setCasesByCountryCity(allMapsByCountryAndRegion);
        caseData.setCasesByCountryAndDate(mapByCountry);
        return caseData;
    }

    @Bean
    public Cases covidData() {
        return getData(null);

    }
}
