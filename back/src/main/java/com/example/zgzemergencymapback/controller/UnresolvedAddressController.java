package com.example.zgzemergencymapback.controller;

import com.example.zgzemergencymapback.model.UnresolvedAddress;
import com.example.zgzemergencymapback.repository.UnresolvedAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/unresolved-addresses")
public class UnresolvedAddressController {

    @Autowired
    private UnresolvedAddressRepository unresolvedAddressRepository;

    @GetMapping
    public ResponseEntity<List<String>> getAllUnresolvedAddresses() {
        List<String> addresses = unresolvedAddressRepository.findAll()
                .stream()
                .map(UnresolvedAddress::getAddress)
                .collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }
}
