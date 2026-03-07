package com.example.zgzemergencymapback.utils.impl;

import com.example.zgzemergencymapback.model.*;
import com.example.zgzemergencymapback.model.incident.Incident;
import com.example.zgzemergencymapback.model.incident.IncidentStatusEnum;
import com.example.zgzemergencymapback.service.GeocodingService;
import com.example.zgzemergencymapback.service.IncidentResourceService;
import com.example.zgzemergencymapback.service.IncidentService;
import com.example.zgzemergencymapback.service.impl.ResourceServiceImpl;
import com.example.zgzemergencymapback.utils.JsonConverterService;
import com.example.zgzemergencymapback.utils.MarkerIconSelector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.example.zgzemergencymapback.utils.AdressUtils.*;


@Service
public class JsonConverterServiceImpl implements JsonConverterService {
    @Autowired
    private ResourceServiceImpl resourceService;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private IncidentResourceService incidentResourceService;

    @Autowired
    private com.example.zgzemergencymapback.repository.UnresolvedAddressRepository unresolvedAddressRepository;


    /*
     * Método que obtiene datos del json para crear objetos incident
     * y determinar si es necesario guardarlos en la base de datos
     */
    public List<Incident> getIncidentInfoFromJson(String json, IncidentStatusEnum status) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper.readTree(json);
        JsonNode resultNode = root.path("result");

        List<Incident> incidentList = new ArrayList<>();
        for (JsonNode node : resultNode) {
            String fecha = node.path("fecha").asText();
            String[] dateTime = fecha.split("T");
            LocalDate date = LocalDate.parse(dateTime[0]);
            LocalTime time = LocalTime.parse(dateTime[1]);
            String duration = node.path("duracion").asText();

            Incident incident = Incident.builder()
                    .date(date)
                    .time(time)
                    .status(status)
                    .build();

            Optional<Incident> incidentOptional = incidentService.getIncidentByDateAndTime(incident.getDate(), incident.getTime());
            // Si no hay ningun incident en la base de datos con esa fecha y hora
            // se termina de crear el objeto y guardar en la base de datos
            if(incidentOptional.isEmpty()){
                // Completar los datos del incidente
                incident = completeIncidentDataFromJson(incident, node);
                if(incident != null){
                    incidentList.add(incident);
                }

            }
            // Si existe un incident en la base de datos con esa fecha y hora
            // pero estaba abierto -> y la info del json indica que se ha
            // cerrado actualizamos su status y establecemos duracion
            else if(incidentOptional.isPresent()
                    && incidentOptional.get().getStatus().equals(IncidentStatusEnum.OPEN)
                    && incident.getStatus().equals(IncidentStatusEnum.CLOSED)
            ){
                Incident incidentToUpdate = incidentOptional.get();
                incidentToUpdate.setStatus(IncidentStatusEnum.CLOSED);
                incidentToUpdate.setDuration(duration);
                incidentService.saveIncident(incidentToUpdate);
                incidentList.add(incidentToUpdate);
            } else{
                incidentList.add(incidentOptional.get());
                System.out.println("Este incidente ya estaba previamente registrado " +
                        "en la base de datos con fecha: " + date + " y hora: " + time + " y estado: " + status);
            }
        }
        return incidentList;
    }




    public Incident completeIncidentDataFromJson(Incident incident, JsonNode node) {
        String incidentType = node.path("tipoSiniestro").asText();
        incident.setIncidentType(incidentType);

        String markerIcon = MarkerIconSelector.selectIcon(incidentType.trim().toLowerCase());
        incident.setMarkerIcon(markerIcon);

        String address = node.path("direccion").asText();

        String duration = node.path("duracion").asText();
        incident.setDuration(duration);


        String coordinatesJsonResponse = geocodingService.getcoordinates(address);
        CoordinatesAndAddress coordinatesAndAddress = getCoordinatesFromJson(coordinatesJsonResponse);

        // Manejar los casos en los que la api de google maps no devuelve la direccion de calle concreta, sino una generica de zaragoza
        if(!isAddresValid(coordinatesAndAddress)) {
            // Agrega en base de datos la calle que no ha podido ser procesada 'address'
            Optional<UnresolvedAddress> existing = unresolvedAddressRepository.findByAddress(address);
            if(existing.isEmpty()) {
                UnresolvedAddress unresolvedAddress = UnresolvedAddress.builder()
                        .address(address)
                        .build();
                unresolvedAddressRepository.save(unresolvedAddress);
            }
        }
        // Guardar las nuevas coordenadas en el set general para evitar tener 2 incidentes con las mismas coordenadas
        if(incidentService.getIncidentByDateAndCoordinates(
                incident.getDate(),
                coordinatesAndAddress.getCoordinates().get(0),
                coordinatesAndAddress.getCoordinates().get(1))
                .isPresent()){
            coordinatesAndAddress = adjustCoordinates(coordinatesAndAddress);
        }


        Double latitude = coordinatesAndAddress.getCoordinates().get(0);
        Double longitude = coordinatesAndAddress.getCoordinates().get(1);
        incident.setLatitude(latitude);
        incident.setLongitude(longitude);

        address = coordinatesAndAddress.getAddress();
        incident.setAddress(address);

        List<Resource> resourceList = new ArrayList<>();
        JsonNode resourcesNode = node.path("recursos");
        for (JsonNode resourceNode : resourcesNode) {
            resourceService.checkResource(resourceNode.asText());
            Resource resource = resourceService.findResourceByName(resourceNode.asText());
            resourceList.add(resource);
        }

        //guardar incident sin resources para poder añadirlos después
        incidentService.saveIncident(incident);
        //agregar filas con las relaciones entre incident y resources
        incidentResourceService.addResourceToIncident(incident, resourceList);

        //obtener las relaciones entre incident y resources
        List<IncidentResource> incidentResourceList =  incidentResourceService.findIncidentResourceByIncident(incident);
        //añadir las relaciones al incident
        incident.setIncidentResources(incidentResourceList);
        //guardar el incident con las relaciones
        incidentService.saveIncident(incident);

        return incident;
    }


    public static CoordinatesAndAddress getCoordinatesFromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JsonNode featuresNode = root.path("features");
        if (featuresNode.isMissingNode() || !featuresNode.isArray() || featuresNode.isEmpty()) {
            return CoordinatesAndAddress.builder()
                    .coordinates(Arrays.asList(0.0, 0.0))
                    .address("")
                    .build();
        }

        JsonNode firstFeature = featuresNode.get(0);
        JsonNode geometry = firstFeature.path("geometry");
        JsonNode coords = geometry.path("coordinates");

        // GeoJSON uses [longitude, latitude]
        double lng = coords.path(0).asDouble();
        double lat = coords.path(1).asDouble();

        List<Double> coordinates = Arrays.asList(lat, lng);

        JsonNode properties = firstFeature.path("properties");
        String address = "";
        
        if (properties.has("name") && !properties.path("name").asText().isEmpty()) {
            address = properties.path("name").asText();
        } else if (properties.has("street") && !properties.path("street").asText().isEmpty()) {
            address = properties.path("street").asText();
        } else {
            // Un fallback por si no tiene 'name' ni 'street' pero sí tiene 'city' o algo, o queda vacío
            address = properties.path("city").asText("");
        }

        return CoordinatesAndAddress.builder()
                .coordinates(coordinates)
                .address(address)
                .build();
    }



}
