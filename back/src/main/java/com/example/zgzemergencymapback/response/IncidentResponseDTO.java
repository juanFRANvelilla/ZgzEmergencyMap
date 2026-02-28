package com.example.zgzemergencymapback.response;

import com.example.zgzemergencymapback.model.incident.Incident;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponseDTO {
    private String date;
    private int size;
    private List<Incident> incidentList;
}
