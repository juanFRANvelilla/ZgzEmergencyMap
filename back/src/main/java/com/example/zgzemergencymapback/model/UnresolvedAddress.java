package com.example.zgzemergencymapback.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;

import java.util.UUID;

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

}
