package com.example.zgzemergencymapback.service.impl;

import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.repository.IncidentRepository;
import com.example.zgzemergencymapback.response.IncidentResponseDTO;
import com.example.zgzemergencymapback.service.IncidentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class IncidentServiceImpl implements IncidentService {
    @Autowired
    private IncidentRepository incidentRepository;

    public Optional<Incident> getIncidentByDateAndTime(LocalDate date, LocalTime time, String incidentType) {
        return incidentRepository.findByDateAndTime(date, time, incidentType);
    }

    public Optional<Incident> getIncidentByDateAndCoordinates(LocalDate date, Double latitude, Double longitude) {
        return incidentRepository.findIncidentByDateAndCoordinates(date, latitude, longitude);
    }

    /*
     * Método que obtiene todas las incidencias de una fecha indicada
     */
    public IncidentResponseDTO getIncidentByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<Incident> incidentList = incidentRepository.findIncidentByDate(localDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return IncidentResponseDTO
                .builder()
                .date(localDate.format(formatter))
                .size(incidentList.size())
                .incidentList(incidentList)
                .build();
    }


    /*
     * Método que obtiene todas las incidencias del dia de hoy, y aquellas que siguen abiertas
     */
    public IncidentResponseDTO getTodayIncidentData() {
        LocalDate date = LocalDate.now();
        List<Incident> incidentList = incidentRepository.findTodayIncident(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return IncidentResponseDTO
                .builder()
                .date(date.format(formatter))
                .size(incidentList.size())
                .incidentList(incidentList)
                .build();
    }


    /*
     * Método que obtiene todas las incidencias situadas dentro de un polígono,
     * ordenadas de más reciente a menos reciente. Primero se filtra en BD por el
     * rectángulo que envuelve al polígono y después se descartan en memoria las
     * que caen dentro del rectángulo pero fuera del polígono real.
     */
    public IncidentResponseDTO getIncidentByPolygon(List<List<Double>> coordinates) {
        if (coordinates == null || coordinates.size() < 3
                || coordinates.stream().anyMatch(point -> point == null || point.size() < 2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El polígono debe tener al menos 3 vértices con formato [longitud, latitud]");
        }

        Path2D.Double polygon = new Path2D.Double();
        polygon.moveTo(coordinates.get(0).get(0), coordinates.get(0).get(1));
        for (int i = 1; i < coordinates.size(); i++) {
            polygon.lineTo(coordinates.get(i).get(0), coordinates.get(i).get(1));
        }
        polygon.closePath();

        Rectangle2D bounds = polygon.getBounds2D();
        List<Incident> incidentList = incidentRepository.findIncidentInBoundingBox(
                        bounds.getMinY(), bounds.getMaxY(), bounds.getMinX(), bounds.getMaxX())
                .stream()
                .filter(incident -> polygon.contains(incident.getLongitude(), incident.getLatitude()))
                .toList();

        return IncidentResponseDTO
                .builder()
                .size(incidentList.size())
                .incidentList(incidentList)
                .build();
    }


    public void saveIncident(Incident incident) {
        incidentRepository.save(incident);
    }

    @Transactional
    public void deleteAllIncident(){
        incidentRepository.deleteAll();
    }

    /*
     * Método maneja la situacion en la que existen incidencias abiertas,
     * pero en la api de bomberos desaparecen, es decir dejan de aparecer
     * como abiertas, pero tampoco aparecen como cerradas
     */
    @Transactional
    public void handleLostIncidents(List<Incident> openIncidentList) {
        // A traves de la lista de incidentes 'openIncidentList' que se
        // corresponden a los ultimos incidentes abiertos obtenidos con
        // la api buscaremos en la db aquellos incidentes abiertos que
        // no se corresponden con los ultimos obtenidos
        List<java.util.UUID> realOpenIncidentIds = new ArrayList<>();

        if(openIncidentList.size() == 0){
            // En el caso que en la ultima llamada api no aparezcan incidentes abiertos se cierran todos
            incidentRepository.closeAllOpenIncident();
        } else {
            // Si hay algun incidente abierto en la ultima llamada api se cerraran aquellos
            // abiertos que no coincidan con el id de estos ultimos
            for (Incident incident : openIncidentList) {
                realOpenIncidentIds.add(incident.getId());
            }
            incidentRepository.closeOtherOpenIncident(realOpenIncidentIds);
        }
    }

}
