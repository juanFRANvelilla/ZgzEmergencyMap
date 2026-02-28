package com.example.zgzemergencymapback.utils;

import com.example.zgzemergencymapback.model.incident.MarkerIconEnum;

import java.text.Normalizer;

public class MarkerIconSelector {
    public static String selectIcon(String incidentString) {
        incidentString = Normalizer.normalize(incidentString, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        boolean hasNoFire = incidentString.contains("sin incendio");

        if (incidentString.contains("incendio") && !hasNoFire){
            return MarkerIconEnum.FIRE.toString();
        } else if (incidentString.contains("arboles")) {
            return MarkerIconEnum.TREE.toString();
        } else if (incidentString.contains("animales")) {
            return MarkerIconEnum.ANIMALS.toString();
        } else if (incidentString.contains("construccion")) {
            return MarkerIconEnum.CONSTRUCTION.toString();
        } else if (incidentString.contains("productos peligrosos")) {
            return MarkerIconEnum.DANGEROUSPRODUCT.toString();
        } else if (incidentString.contains("apertura de puertas")) {
            return MarkerIconEnum.BLOCKED.toString();
        } else if (incidentString.contains("ascensor")) {
            return MarkerIconEnum.ELEVATOR.toString();
        } else if (incidentString.contains("trafico")) {
            return MarkerIconEnum.TRAFFIC.toString();
        } else if (incidentString.contains("achique de agua")) {
            return MarkerIconEnum.WATERDRAINAGE.toString();
        } else {
            return MarkerIconEnum.DEFAULT.toString();
        }
    }
}
