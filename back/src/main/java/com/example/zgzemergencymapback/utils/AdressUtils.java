package com.example.zgzemergencymapback.utils;

import com.example.zgzemergencymapback.model.CoordinatesAndAddress;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class AdressUtils {
    private static final Path2D ZARAGOZA_POLYGON = new Path2D.Double();
    private static final double LATITUDE_OFFSET = 0.0000;
    private static final double LONGITUDE_OFFSET = 0.0003;

    static {
        // NW (Noroeste) - Cerca de Alagón / Tauste
        ZARAGOZA_POLYGON.moveTo(41.8500, -1.2000);
        // NE (Noreste) - Cerca de San Mateo de Gállego / Alfajarín
        ZARAGOZA_POLYGON.lineTo(41.8500, -0.6000);
        // SE (Sureste) - Cerca de Fuentes de Ebro / Mediana de Aragón
        ZARAGOZA_POLYGON.lineTo(41.4500, -0.6000);
        // SW (Suroeste) - Cerca de La Muela / Botorrita
        ZARAGOZA_POLYGON.lineTo(41.4500, -1.2000);
        ZARAGOZA_POLYGON.closePath();
    }

    public static boolean isAddresValid(CoordinatesAndAddress coordinatesAndAddress) {
        double latitude = coordinatesAndAddress.getCoordinates().get(0);
        double longitude = coordinatesAndAddress.getCoordinates().get(1);
        return ZARAGOZA_POLYGON.contains(latitude, longitude)
                && (coordinatesAndAddress.getCoordinates().get(0) != 41.6474339
                        || coordinatesAndAddress.getCoordinates().get(1) != -0.8861451);
    }

    public static CoordinatesAndAddress adjustCoordinates(CoordinatesAndAddress original) {
        // Obtener las coordenadas originales
        List<Double> originalCoordinates = original.getCoordinates();
        if (originalCoordinates == null || originalCoordinates.size() < 2) {
            throw new IllegalArgumentException("Invalid coordinates list");
        }

        // Crear una nueva lista de coordenadas con el desplazamiento
        List<Double> adjustedCoordinates = new ArrayList<>();
        double latitude = originalCoordinates.get(0);
        double longitude = originalCoordinates.get(1);

        // Aplicar desplazamiento a las coordenadas
        adjustedCoordinates.add(latitude + LATITUDE_OFFSET);
        adjustedCoordinates.add(longitude + LONGITUDE_OFFSET);

        // Crear y devolver una nueva instancia con las coordenadas ajustadas
        return CoordinatesAndAddress.builder()
                .coordinates(adjustedCoordinates)
                .address(original.getAddress()) // Mantener la misma dirección
                .build();
    }

}
