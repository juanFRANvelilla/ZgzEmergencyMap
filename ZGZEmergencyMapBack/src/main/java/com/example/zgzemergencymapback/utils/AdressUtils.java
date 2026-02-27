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
        ZARAGOZA_POLYGON.moveTo(41.75731894412275, -0.8926774895802142);
        ZARAGOZA_POLYGON.lineTo(41.6771110403174, -0.715725563529871);
        ZARAGOZA_POLYGON.lineTo(41.5660117933122, -0.8263873420568228);
        ZARAGOZA_POLYGON.lineTo(41.619587124136395, -1.0835289626919138);
        ZARAGOZA_POLYGON.lineTo(41.75372968626021, -1.0445033113273365);
        ZARAGOZA_POLYGON.closePath();
    }

    public static boolean isAddresValid(CoordinatesAndAddress coordinatesAndAddress) {
        double latitude = coordinatesAndAddress.getCoordinates().get(0);
        double longitude = coordinatesAndAddress.getCoordinates().get(1);
        return ZARAGOZA_POLYGON.contains(latitude, longitude)
                && (coordinatesAndAddress.getCoordinates().get(0) != 41.6474339 || coordinatesAndAddress.getCoordinates().get(1) != -0.8861451);
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




    public static String formatAddress(String address) {
//        address = "FEDERICO *escribir* OZANAM, FEDERICO (Zaragoza) esto es una, sdfsafe .cadena muy raaa, dsjfdslfj &*&(^*(&(*& Ñ";
        // Primero, eliminamos cualquier texto adicional como "*escribir*"
        String cleanedInput = address.replaceAll("\\*.*\\*", "").trim();

        // Luego, dividimos la cadena usando ", " como delimitador
        String[] parts = cleanedInput.split(", ");

        // Verifica que la división haya tenido éxito
        if (parts.length < 2) {
            return address;
        }

        // La primera parte contiene la ubicación y la segunda parte contiene el prefijo junto con la ciudad
        String location = parts[0].trim(); // "OZANAN"
        String remaining = parts[parts.length - 1].trim(); // "FEDERICO *escribir* OZANAM, FEDERICO (Zaragoza)"

        // Encontramos el índice del primer paréntesis
        int parenthesisIndex = remaining.indexOf('(');

        if (parenthesisIndex == -1) {
            return address;
        }

        // Extraemos el prefijo y la ciudad
        String prefix = remaining.substring(0, parenthesisIndex).trim(); // "FEDERICO *escribir* OZANAM,"
        String city = remaining.substring(parenthesisIndex + 1).replace(")", "").trim(); // "Zaragoza"

        // Limpiamos cualquier texto adicional antes de la ciudad
        prefix = prefix.replaceAll(",.*", "").trim(); // "FEDERICO"

        // Reensamblamos la cadena formateada
        return prefix + " " + location + " (" + city + ")";
    }



    public static String formatAddress2(String address) {
//        address = "FEDERICO *escribir* OZANAM, FEDERICO (Zaragoza) esto es una, sdfsafe .cadena muy raaa, dsjfdslfj &*&(^*(&(*& Ñ";
        // Primero, eliminamos cualquier texto adicional como "*escribir*"
        String cleanedInput = address.replaceAll("\\*.*\\*", "").trim();

        // Luego, dividimos la cadena usando ", " como delimitador
        String[] parts = cleanedInput.split(", ");

        // Verifica que la división haya tenido éxito
        if (parts.length < 2) {
            return address;
        }

        // La primera parte contiene la ubicación y la segunda parte contiene el prefijo junto con la ciudad
        String location = parts[0].trim(); // "OZANAN"
        String remaining = parts[parts.length - 1].trim(); // "FEDERICO *escribir* OZANAM, FEDERICO (Zaragoza)"

        // Encontramos el índice del primer paréntesis
        int parenthesisIndex = remaining.indexOf('(');

        if (parenthesisIndex == -1) {
            return address;
        }

        String city = remaining.substring(parenthesisIndex + 1).replace(")", "").trim(); // "Zaragoza"


        return  "calle " + location + " (" + city + ")";
    }
}
