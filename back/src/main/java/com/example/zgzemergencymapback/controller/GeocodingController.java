package com.example.zgzemergencymapback.controller;

import com.example.zgzemergencymapback.service.GeocodingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/api/geocode/test")
    public java.util.Map<String, Object> testGeocode(@RequestParam String address) {
        String normalized = geocodingService.normalizeAddressForTest(address);
        String apiResponse = geocodingService.getcoordinates(address);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("normalizedAddress", normalized);
        
        try {
            response.put("apiResponse", new com.fasterxml.jackson.databind.ObjectMapper().readTree(apiResponse));
        } catch (Exception e) {
            response.put("apiResponse", apiResponse);
        }
        
        return response;
    }
}
