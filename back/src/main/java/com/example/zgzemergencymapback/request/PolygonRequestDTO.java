package com.example.zgzemergencymapback.request;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolygonRequestDTO {
    // Vértices del polígono en formato GeoJSON: cada elemento es [longitud, latitud]
    private List<List<Double>> coordinates;
}
