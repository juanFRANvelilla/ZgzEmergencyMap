package com.example.zgzemergencymapback.model;

import com.example.zgzemergencymapback.model.incident.IncidentStatusEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/*
 * Representa un incidente cuya dirección no se ha podido geocodificar todavía
 * dentro del término de Zaragoza. Guardamos TODOS los datos originales del
 * parte de bomberos (no solo la dirección) para poder ver, sin necesidad de
 * mirar directamente los logs, qué incidente real hay detrás de cada
 * dirección pendiente: cuándo ocurrió, de qué tipo era, qué recursos acudieron,
 * etc. No hay un job de reintento explícito: si el incidente sigue apareciendo
 * como abierto o cerrado "hoy" en el parte de bomberos, el propio ScheduledTask
 * (cada 30 min) vuelve a intentar geocodificarlo de forma natural.
 */
@Entity
@Table(name = "unresolved_address")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnresolvedAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "address")
    private String address;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "incident_type")
    private String incidentType;

    @Column(name = "duration")
    private String duration;

    @Column(name = "marker_icon")
    private String markerIcon;

    @Column(name = "status")
    private IncidentStatusEnum status;

    // Nombres de los recursos separados por "|" para poder reconstruir el incidente
    @Column(name = "resources", length = 1000)
    private String resources;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

}
