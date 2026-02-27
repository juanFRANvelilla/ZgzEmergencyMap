package com.example.zgzemergencymapback.service;

import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.response.IncidentResponseDTO;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface IncidentService {
    Optional<Incident> getIncidentByDateAndTime(LocalDate date, LocalTime time);

    Optional<Incident> getIncidentByDateAndCoordinates(LocalDate date, Double latitude, Double longitude);

    IncidentResponseDTO getIncidentByDate(String date);

    IncidentResponseDTO getTodayIncidentData();

    void saveIncident(Incident incident);

    @Transactional
    void deleteAllIncident();

    @Transactional
    void handleLostIncidents(List<Incident> openIncidentList);
}
