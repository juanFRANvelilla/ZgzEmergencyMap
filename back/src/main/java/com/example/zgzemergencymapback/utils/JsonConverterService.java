package com.example.zgzemergencymapback.utils;

import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.model.incident.IncidentStatusEnum;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;


public interface JsonConverterService {
    List<Incident> getIncidentInfoFromJson(String json, IncidentStatusEnum status) throws IOException;

    Incident completeIncidentDataFromJson(Incident incident, JsonNode node);

    /*
     * Reintenta geocodificar las direcciones que quedaron pendientes en
     * "unresolved_address". Si ahora se resuelven dentro de Zaragoza, reconstruye
     * y guarda el Incident completo (con sus recursos) usando los datos originales
     * del parte que se guardaron junto a la dirección, y elimina el pendiente.
     * Devuelve el número de incidentes recuperados.
     */
    int retryUnresolvedAddresses();

}
