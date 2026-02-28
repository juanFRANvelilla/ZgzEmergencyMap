package com.example.zgzemergencymapback.service;

import com.example.zgzemergencymapback.model.Resource;
import jakarta.transaction.Transactional;

public interface ResourceService {
    void checkResource(String name);

    Resource findResourceByName(String name);

    @Transactional
    void deleteAllResources();
}
