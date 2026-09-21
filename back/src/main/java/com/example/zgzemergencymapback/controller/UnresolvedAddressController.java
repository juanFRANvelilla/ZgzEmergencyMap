package com.example.zgzemergencymapback.controller;

import com.example.zgzemergencymapback.model.UnresolvedAddress;
import com.example.zgzemergencymapback.repository.UnresolvedAddressRepository;
import com.example.zgzemergencymapback.utils.JsonConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/unresolved-addresses")
public class UnresolvedAddressController {

    @Autowired
    private UnresolvedAddressRepository unresolvedAddressRepository;

    @Autowired
    private JsonConverterService jsonConverterService;

    @GetMapping
    public ResponseEntity<List<String>> getAllUnresolvedAddresses() {
        List<String> addresses = unresolvedAddressRepository.findAll()
                .stream()
                .map(UnresolvedAddress::getAddress)
                .collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }

    /*
     * Dispara manualmente el reintento de geocodificación de las direcciones
     * pendientes (lo mismo que hace el cron diario), útil para recuperar el
     * backlog justo después de desplegar una mejora en el normalizador de
     * direcciones sin tener que esperar a la próxima ejecución programada.
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, Integer>> retryUnresolvedAddresses() {
        int recovered = jsonConverterService.retryUnresolvedAddresses();
        return ResponseEntity.ok(Map.of("recoveredIncidents", recovered));
    }
}
