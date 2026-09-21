package com.example.zgzemergencymapback.task;

import com.example.zgzemergencymapback.service.impl.IncidentsZgzDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {

    @Autowired
    private IncidentsZgzDataServiceImpl incidentsZgzDataService;

    // Nota: no hace falta un job de reintento aparte para las direcciones
    // pendientes en unresolved_address. Mientras el incidente siga apareciendo
    // como abierto o cerrado "hoy" en el parte de bomberos, esta misma tarea
    // (cada 30 min) vuelve a llamar a getIncidentInfoFromJson, que comprueba
    // que no existe en la tabla incident y reintenta geocodificarlo desde cero.
    @Scheduled(fixedRate = 1800000)
    public void executeTask() {
        System.out.println("Ejecutar tarea programada");
        incidentsZgzDataService.reloadTodayEmergency();
    }
}
