package com.example.zgzemergencymapback.utils;

import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.model.incident.IncidentStatusEnum;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;


public interface JsonConverterService {
    List<Incident> getIncidentInfoFromJson(String json, IncidentStatusEnum status) throws IOException;

    Incident completeIncidentDataFromJson(Incident incident, JsonNode node);

}
