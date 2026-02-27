package com.example.zgzemergencymapback.service.impl;

import com.example.zgzemergencymapback.model.Resource;
import com.example.zgzemergencymapback.repository.ResourceRepository;
import com.example.zgzemergencymapback.service.ResourceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void checkResource(String name) {
        Resource resource = resourceRepository.findByName(name);
        if (resource == null) {
            resource = Resource.builder().name(name).build();
            resourceRepository.save(resource);
        }
    }

    public Resource findResourceByName(String name) {
        return resourceRepository.findByName(name);
    }

    @Transactional
    public void deleteAllResources() {
        resourceRepository.deleteAll();
        jdbcTemplate.execute("ALTER SEQUENCE resource_id_seq RESTART WITH 1");
    }

}
