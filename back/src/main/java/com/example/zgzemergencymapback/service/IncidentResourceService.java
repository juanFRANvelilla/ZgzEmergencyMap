package com.example.zgzemergencymapback.service;

import com.example.zgzemergencymapback.model.IncidentResource;
import com.example.zgzemergencymapback.model.Resource;
import com.example.zgzemergencymapback.model.incident.Incident;
import jakarta.transaction.Transactional;

import java.util.List;

public interface IncidentResourceService {
    void addResourceToIncident(Incident incident, List<Resource> resourceList);

    List<IncidentResource> findIncidentResourceByIncident(Incident incident);

    @Transactional
    void deleteAllIncidentResource();
}
