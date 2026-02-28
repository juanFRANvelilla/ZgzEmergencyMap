package com.example.zgzemergencymapback.service.impl;

import com.example.zgzemergencymapback.model.IncidentResource;
import com.example.zgzemergencymapback.model.Resource;
import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.repository.IncidentResourceRepository;
import com.example.zgzemergencymapback.service.IncidentResourceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentResourceServiceImpl implements IncidentResourceService {
    @Autowired
    private IncidentResourceRepository incidentResourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void addResourceToIncident(Incident incident, List<Resource> resourceList) {
        for (Resource resource : resourceList) {
            IncidentResource incidentResource = new IncidentResource();
            incidentResource.setIncident(incident);
            incidentResource.setResource(resource);
            incidentResourceRepository.save(incidentResource);
        }
    }

    public List<IncidentResource> findIncidentResourceByIncident(Incident incident) {
        return incidentResourceRepository.findByIncident(incident);
    }

    @Transactional
    public void deleteAllIncidentResource() {
        incidentResourceRepository.deleteAll();
        jdbcTemplate.execute("ALTER SEQUENCE incident_resource_id_seq RESTART WITH 1");
    }

}
