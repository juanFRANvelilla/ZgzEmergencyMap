package com.example.zgzemergencymapback.model;
import com.example.zgzemergencymapback.model.incident.Incident;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "incident_resource")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    @JsonBackReference
    private Incident incident;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

}