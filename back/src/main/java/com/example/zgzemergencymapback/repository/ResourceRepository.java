package com.example.zgzemergencymapback.repository;

import com.example.zgzemergencymapback.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Resource findByName(String name);
}