package com.example.zgzemergencymapback.service.impl;

import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.model.incident.IncidentStatusEnum;
import com.example.zgzemergencymapback.service.IncidentService;
import com.example.zgzemergencymapback.service.IncidentsZgzDataService;
import com.example.zgzemergencymapback.utils.impl.JsonConverterServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class IncidentsZgzDataServiceImpl implements IncidentsZgzDataService {
    private final RestTemplate restTemplate;

    @Autowired
    private JsonConverterServiceImpl jsonConverterService;

    @Autowired
    IncidentService incidentService;

    public IncidentsZgzDataServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /*
      * Método que obtiene los datos de los incidentes de la API de Zaragoza
      * y los guarda en la base de datos, devuelve la lista de incident que se han
      * introducido en la base de datos o incidentes que se han cerrado
     */
    public List<Incident> reloadTodayEmergency() {
        String url = "https://www.zaragoza.es/sede/servicio/bomberos?tipo=20&rf=markdown";
        List<Incident> incidentList = new ArrayList<>();
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            // Convertir json a objetos incident cerrados, y llevar a cabo la logica para
            // guardar en la base de datos, devuelve la lista de incident que se han decodificado
            incidentList = jsonConverterService.getIncidentInfoFromJson(jsonResponse, IncidentStatusEnum.CLOSED);


            url = "https://www.zaragoza.es/sede/servicio/bomberos?tipo=10&rf=markdown";
            jsonResponse = restTemplate.getForObject(url, String.class);
            // Convertir el JSON a objetos Incident abiertos, y llevar a cabo la logica para
            // guardar en la base de datos, devuelve la lista de incident que se han decodificado
            List<Incident> openIncidentList = jsonConverterService.getIncidentInfoFromJson(jsonResponse, IncidentStatusEnum.OPEN);
            incidentList.addAll(openIncidentList);

            incidentService.handleLostIncidents(openIncidentList);

        } catch (RestClientException e) {
            System.err.println("Error al hacer la llamada a la API: " + e.getMessage());
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // Maneja errores relacionados con la conversión del JSON
            System.err.println("Error al procesar el JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Maneja cualquier otro tipo de error no previsto
            System.err.println("Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
        return incidentList;
    }



    public List<Incident> reloadYesterdayEmergency() {
        String url = "https://www.zaragoza.es/sede/servicio/bomberos?tipo=21&rf=markdown";
        List<Incident> incidentList = new ArrayList<>();
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            incidentList = jsonConverterService.getIncidentInfoFromJson(jsonResponse, IncidentStatusEnum.CLOSED);

        } catch (RestClientException e) {
            System.err.println("Error al hacer la llamada a la API: " + e.getMessage());
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // Maneja errores relacionados con la conversión del JSON
            System.err.println("Error al procesar el JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Maneja cualquier otro tipo de error no previsto
            System.err.println("Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
        return incidentList;
    }

}
