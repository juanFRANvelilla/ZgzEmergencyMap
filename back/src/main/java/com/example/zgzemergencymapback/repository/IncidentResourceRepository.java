package com.example.zgzemergencymapback.repository;

import com.example.zgzemergencymapback.model.IncidentResource;
import com.example.zgzemergencymapback.model.incident.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentResourceRepository extends JpaRepository<IncidentResource, UUID> {
    @Query("SELECT ir FROM IncidentResource ir WHERE ir.incident = :incident")
    List<IncidentResource> findIncidentResourceByIncident(@Param("incident") Incident incident);
}
