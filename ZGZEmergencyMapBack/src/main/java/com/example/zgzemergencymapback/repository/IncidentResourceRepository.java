package com.example.zgzemergencymapback.repository;

import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.model.IncidentResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentResourceRepository extends JpaRepository<IncidentResource, Long> {
    // Método para encontrar todos los resources asociados con el incident
    List<IncidentResource> findByIncident(Incident incident);
}
