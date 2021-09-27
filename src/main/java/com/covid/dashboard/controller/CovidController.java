package com.covid.dashboard.controller;

import java.util.Map;

import com.covid.dashboard.constants.CovidDashBoardConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.covid.dashboard.model.Case;
import com.covid.dashboard.model.CovidResponse;
import com.covid.dashboard.dao.CovidCasesDao;
import com.covid.dashboard.service.CovidCasesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RestController
@Api(value = "Covid Dashboard", produces = MediaType.APPLICATION_JSON_VALUE, tags = {"Covid Dashboard Controller"})
public class CovidController {

    @Autowired
    private CovidCasesDao dao;
    @Autowired
    private CovidCasesService service;

    @ApiOperation(value = "API will populate country wise or region wise data as passed in query")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CovidResponse.class),
            @ApiResponse(code = 400, message = "Invalid request parameter"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @GetMapping(value = "covid/cases")
    public CovidResponse findByCountryOrState(@RequestParam("query") String query) {
        log.info("Inside findByCountryOrState: this method will get data country and state wise");
        ObjectMapper map = new ObjectMapper();
        try {
            Map<?, ?> queryData = map.readValue(query, Map.class);
            String type = (String) queryData.get(CovidDashBoardConstants.TYPE);
            log.info("Type from request:" + type);
            if (type.equalsIgnoreCase(CovidDashBoardConstants.COUNTRY)) {
                log.info("Country name:" + (String) queryData.get(CovidDashBoardConstants.COUNTRY));
                return service.getCaseCountry(queryData);
            } else {
                return service.getCaseCountryCity(queryData);
            }

        } catch (JsonMappingException e) {
            log.error(e.getMessage());
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @ApiOperation(value = "API will populate all country wise covid related data as passed in query")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CovidResponse.class),
            @ApiResponse(code = 400, message = "Invalid request parameter"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @GetMapping(value = "covid/cases/alldata")
    public Map<String, Case> getAllData(@RequestParam("query") String query) {
        return dao.getData(null).getCasesByCountry();
    }

    @ApiOperation(value = "API will populate Covid data as per filters passed in query like custom date, country,state,frequency ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CovidResponse.class),
            @ApiResponse(code = 400, message = "Invalid request parameter"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @GetMapping(value = "covid/cases/filters")
    public CovidResponse getCovidDataByFilters(@RequestParam("query") String query) {
        ObjectMapper map = new ObjectMapper();
        Map<?, ?> queryData = null;
        try {
            queryData = map.readValue(query, Map.class);
        } catch (JsonProcessingException e) {
			log.error(e.getMessage());
        }
        return service.getCovidDataByFilters(queryData);
    }


}
