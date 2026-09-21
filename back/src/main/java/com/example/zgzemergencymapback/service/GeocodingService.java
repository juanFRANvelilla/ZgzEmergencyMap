package com.example.zgzemergencymapback.service;

public interface GeocodingService {
    String getcoordinates(String address);
    String normalizeAddressForTest(String address);
}
