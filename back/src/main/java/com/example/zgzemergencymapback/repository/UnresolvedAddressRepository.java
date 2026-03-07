package com.example.zgzemergencymapback.repository;

import com.example.zgzemergencymapback.model.UnresolvedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnresolvedAddressRepository extends JpaRepository<UnresolvedAddress, UUID> {
    Optional<UnresolvedAddress> findByAddress(String address);
}
