package com.example.zgzemergencymapback.repository;

import com.example.zgzemergencymapback.model.incident.Incident;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    @Query("SELECT i FROM Incident i WHERE i.date = :date AND i.time = :time")
    Optional<Incident> findByDateAndTime(@Param("date") LocalDate date, @Param("time") LocalTime time);

    @Query("SELECT i FROM Incident i WHERE i.date = :date AND i.latitude = :latitude AND i.longitude = :longitude")
    Optional<Incident> findIncidentByDateAndCoordinates(@Param("date") LocalDate date, @Param("latitude") Double latitude, @Param("longitude") Double longitude);

    @Query("SELECT i FROM Incident i WHERE i.date = :date OR i.status = 0")
    List<Incident> findTodayIncident(@Param("date") LocalDate date);

    @Query("SELECT i FROM Incident i WHERE i.date = :date")
    List<Incident> findIncidentByDate(@Param("date") LocalDate date);

    @Modifying
    @Transactional
    @Query("UPDATE Incident i SET i.status = 1 WHERE i.status = 0")
    void closeAllOpenIncident();

    @Modifying
    @Transactional
    @Query("UPDATE Incident i SET i.status = 1 WHERE i.status = 0 AND i.id NOT IN :ids")
    void closeOtherOpenIncident(@Param("ids") List<Long> ids);
}
