package com.example.zgzemergencymapback.model.incident;

import com.example.zgzemergencymapback.model.IncidentResource;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incident")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    @Column(name = "status", nullable = false)
    private IncidentStatusEnum status;

    @Column(name = "incident_type", nullable = false)
    private String incidentType;

    @Column(name = "marker_icon", nullable = false)
    private String markerIcon;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "duration", nullable = false)
    private String duration;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<IncidentResource> incidentResources = new ArrayList<>();

    public void addIncidentResource(IncidentResource incidentResource) {
        incidentResources.add(incidentResource);
        incidentResource.setIncident(this);
    }

}
