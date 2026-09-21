package com.example.zgzemergencymapback.repository;

import com.example.zgzemergencymapback.model.UnresolvedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnresolvedAddressRepository extends JpaRepository<UnresolvedAddress, UUID> {
    Optional<UnresolvedAddress> findByAddress(String address);

    // Direcciones pendientes de reintentar: por debajo del límite de intentos
    // para no seguir llamando indefinidamente a la API gratuita de geocoding
    // con direcciones que probablemente nunca se resuelvan (ej. pruebas de radio).
    @Query("SELECT u FROM UnresolvedAddress u WHERE u.attempts < :maxAttempts")
    List<UnresolvedAddress> findPendingRetries(@Param("maxAttempts") int maxAttempts);
}
