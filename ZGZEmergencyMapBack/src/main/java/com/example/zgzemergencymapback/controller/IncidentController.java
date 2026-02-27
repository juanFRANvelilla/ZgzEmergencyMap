package com.example.zgzemergencymapback.controller;

import com.example.zgzemergencymapback.response.IncidentResponseDTO;
import com.example.zgzemergencymapback.service.IncidentResourceService;
import com.example.zgzemergencymapback.service.IncidentService;
import com.example.zgzemergencymapback.service.impl.IncidentsZgzDataServiceImpl;
import com.example.zgzemergencymapback.service.impl.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IncidentController {

    @Autowired
    private IncidentsZgzDataServiceImpl incidentsZgzDataService;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private ResourceServiceImpl resourceService;

    @Autowired
    private IncidentResourceService incidentResourceService;

    /*
        * Método que obtiene los datos de los incidentes abiertos y cerrados de la API del Ayuntamiento de Zaragoza
        * y devuelve aquellos incident que se han guardado o actualizado en la base de datos
     */
    @GetMapping("/reloadTodayEmergency")
    public Object reloadTodayEmergency() {
        return incidentsZgzDataService.reloadTodayEmergency();
    }

    @GetMapping("/reloadYesterdayEmergency")
    public Object reloadYesterdayEmergency() {
        return incidentsZgzDataService.reloadYesterdayEmergency();
    }

    @GetMapping("/getTodayIncident")
    public IncidentResponseDTO getTodayIncidentData() {
        return incidentService.getTodayIncidentData();
    }

    @GetMapping("/getIncidentByDate")
    public IncidentResponseDTO getIncidentByDate(@Param("date") String date) {
        return incidentService.getIncidentByDate(date);
    }

    @GetMapping("/deleteAll")
    public void deleteAll() {
        incidentResourceService.deleteAllIncidentResource();
        resourceService.deleteAllResources();
        incidentService.deleteAllIncident();
    }

}
