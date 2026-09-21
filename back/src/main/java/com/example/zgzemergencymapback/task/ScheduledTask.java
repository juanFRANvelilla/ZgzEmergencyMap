package com.example.zgzemergencymapback.task;

import com.example.zgzemergencymapback.service.impl.IncidentsZgzDataServiceImpl;
import com.example.zgzemergencymapback.utils.JsonConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {

    @Autowired
    private IncidentsZgzDataServiceImpl incidentsZgzDataService;

    @Autowired
    private JsonConverterService jsonConverterService;

    @Scheduled(fixedRate = 1800000)
    public void executeTask() {
        System.out.println("Ejecutar tarea programada");
        incidentsZgzDataService.reloadTodayEmergency();
    }

    /*
     * Reintenta, una vez al día, geocodificar las direcciones que se quedaron
     * pendientes en "unresolved_address" (ver JsonConverterServiceImpl). Con esto
     * los incidentes cuya dirección no se pudo resolver en su momento (por un
     * fallo puntual de la API gratuita, un formato raro, etc.) se recuperan solos
     * en cuanto la dirección se puede geocodificar, en vez de quedar perdidos
     * para siempre.
     */
    @Scheduled(cron = "0 30 4 * * *")
    public void executeRetryUnresolvedAddresses() {
        int recovered = jsonConverterService.retryUnresolvedAddresses();
        System.out.println("Reintento de direcciones pendientes: " + recovered + " incidentes recuperados");
    }
}
