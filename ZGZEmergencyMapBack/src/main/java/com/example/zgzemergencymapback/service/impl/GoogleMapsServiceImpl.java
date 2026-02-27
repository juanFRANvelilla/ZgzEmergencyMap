package com.example.zgzemergencymapback.service.impl;

import com.example.zgzemergencymapback.service.GoogleMapsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
public class GoogleMapsServiceImpl implements GoogleMapsService {
    private final RestTemplate restTemplate;

    @Value("${google.maps.api-key}")
    private String googleMapsApiKey;

    public GoogleMapsServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /*
     * Método que obtiene las coordenadas de una dirección dada
     */
    public String getcoordinates(String address) {
        String baseUrl = "https://maps.googleapis.com/maps/api/geocode/json";
        String url = baseUrl + "?address=" + address + "&key=" + googleMapsApiKey;

        try {
            return restTemplate.getForObject(url, String.class);

        } catch (RestClientException e) {
            System.err.println("Error al hacer la llamada a la API: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Maneja cualquier otro tipo de error no previsto
            System.err.println("Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }
}
