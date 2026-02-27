package com.example.zgzemergencymapback.service;

import com.example.zgzemergencymapback.model.incident.Incident;
import java.util.List;

public interface IncidentsZgzDataService {
    List<Incident> reloadTodayEmergency();

    List<Incident> reloadYesterdayEmergency();
}
