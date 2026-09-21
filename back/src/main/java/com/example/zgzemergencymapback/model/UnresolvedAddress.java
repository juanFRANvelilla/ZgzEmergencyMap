package com.example.zgzemergencymapback.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * Representa una dirección del parte de bomberos que no se ha podido
 * geocodificar todavía dentro del término de Zaragoza.
 *
 * No hay un job de reintento explícito: si la incidencia sigue apareciendo
 * como abierta o cerrada "hoy" en el parte de bomberos, el propio
 * ScheduledTask (cada 30 min) vuelve a llamar a completeIncidentDataFromJson,
 * que reintenta el geocoding desde cero y actualiza esta MISMA fila
 * (attempts + lastAttemptAt) en vez de crear una nueva.
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

    // Dirección tal cual viene en el campo "direccion" del JSON de bomberos, sin tocar
    @Column(name = "address")
    private String address;

    // Dirección resultante de aplicar normalizeAddress: la query que realmente
    // se manda a la API de coordenadas
    @Column(name = "normalized_address")
    private String normalizedAddress;

    // Veces que se ha intentado geocodificar esta dirección (se incrementa cada
    // vez que el parte de bomberos vuelve a traer la incidencia y sigue fallando)
    @Column(name = "attempts")
    private Integer attempts;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

}
