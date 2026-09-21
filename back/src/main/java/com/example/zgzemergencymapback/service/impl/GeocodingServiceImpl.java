package com.example.zgzemergencymapback.service.impl;

import com.example.zgzemergencymapback.service.GeocodingService;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeocodingServiceImpl implements GeocodingService {
    private final RestTemplate restTemplate;

    public GeocodingServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Códigos internos que usa el parte de bomberos para marcar el barrio rural/pedanía
    // al que pertenece la calle (van pegados al nombre, ej: "PILON, CN. EL  MRL (Miralbueno)").
    // Los usamos tanto para limpiar el texto de la calle como para inferir la localidad
    // cuando no viene indicada entre paréntesis.
    private static final Map<String, String> BARRIO_CODES = new LinkedHashMap<>();
    static {
        BARRIO_CODES.put("MRL", "MIRALBUENO");
        BARRIO_CODES.put("CST", "CASETAS");
        BARRIO_CODES.put("GRP", "GARRAPINILLOS");
        BARRIO_CODES.put("MNT", "MONTAÑANA");
        BARRIO_CODES.put("SIS", "SANTA ISABEL");
        BARRIO_CODES.put("VNO", "VENTA DEL OLIVAR");
        BARRIO_CODES.put("PÑF", "PEÑAFLOR");
        BARRIO_CODES.put("ALF", "ALFOCEA");
        BARRIO_CODES.put("MNZ", "MONZALBARBA");
        BARRIO_CODES.put("SJN", "SAN JUAN DE MOZARRIFAR");
        BARRIO_CODES.put("CRT", "CARTUJA BAJA");
        BARRIO_CODES.put("MVR", "MOVERA");
    }

    // Barrios rurales / pedanías: pertenecen al término municipal de Zaragoza, así que
    // siempre se geocodifican añadiendo "ZARAGOZA", nunca como una localidad aparte.
    private static final Set<String> ZARAGOZA_BARRIOS = Set.of(
            "MIRALBUENO", "CASETAS", "GARRAPINILLOS", "MONTAÑANA", "SANTA ISABEL",
            "VENTA DEL OLIVAR", "PEÑAFLOR", "ALFOCEA", "MONZALBARBA",
            "SAN JUAN DE MOZARRIFAR", "CARTUJA BAJA", "MOVERA", "JUSLIBOL",
            "SAN GREGORIO", "VILLAMAYOR", "ZARAGOZA"
    );

    private static final String[] VIA_TYPES = {
            "AVENIDA", "PLAZA", "PASEO", "GLORIETA", "CARRETERA", "CAMINO", "POLIGONO", "PARQUE"
    };

    @Override
    public String normalizeAddressForTest(String address) {
        return normalizeAddress(address);
    }

    private String normalizeAddress(String address) {
        if (address == null || address.isEmpty()) return "";

        String cleanAddress = address.toUpperCase().replace("\"", "").replace("\\", "");

        // 1. Extraer TODOS los paréntesis. El parte de bomberos usa la convención
        // "CALLE (nota opcional) (Localidad)": el ÚLTIMO paréntesis es casi siempre
        // el barrio/pedanía o el municipio real de la incidencia, mientras que los
        // anteriores suelen ser simples anotaciones ("anchura min 2,4 m", "antiguo
        // Cno. Cementerio", "Sector Centro"...) sin ningún valor geográfico.
        // Antes se cogía SIEMPRE el primer paréntesis (la nota) y se descartaban el
        // resto, incluida la localidad real: eso es lo que rompía el geocoding.
        List<String> brackets = new ArrayList<>();
        Matcher bracketMatcher = Pattern.compile("\\(([^()]*)\\)").matcher(cleanAddress);
        while (bracketMatcher.find()) {
            brackets.add(bracketMatcher.group(1).trim());
        }
        cleanAddress = bracketMatcher.replaceAll("").trim();

        String locality = brackets.isEmpty() ? "" : brackets.get(brackets.size() - 1);

        // 2. Quitar el sufijo de distancia que añade el parte a los municipios de la
        // comarca, ej. "Utebo     13km" -> "Utebo". Ese "13KM" colándose en la query
        // de texto libre era otra causa de que Photon no encontrara nada.
        locality = locality.replaceAll("\\s*\\d+\\s*KM\\.?\\s*$", "").trim();

        // 3. Reordenar el artículo pospuesto: "MUELA LA" -> "LA MUELA",
        // "BURGO DE EBRO EL" -> "EL BURGO DE EBRO"
        Matcher articleMatcher = Pattern.compile("^(.+)\\s+(EL|LA|LOS|LAS)$").matcher(locality);
        if (articleMatcher.matches()) {
            locality = (articleMatcher.group(2) + " " + articleMatcher.group(1)).trim();
        }

        boolean isZaragozaBarrio = locality.isEmpty() || ZARAGOZA_BARRIOS.contains(locality);

        // 4. Quitar del texto los códigos internos de barrio (MRL, CST, GRP...) que
        // aparecen sueltos como si fueran parte del nombre de la calle, y si no
        // había ninguna localidad entre paréntesis, inferirla a partir del código.
        for (Map.Entry<String, String> entry : BARRIO_CODES.entrySet()) {
            Pattern codePattern = Pattern.compile("\\b" + entry.getKey() + "\\b\\.?");
            if (codePattern.matcher(cleanAddress).find()) {
                cleanAddress = codePattern.matcher(cleanAddress).replaceAll("");
                if (locality.isEmpty()) {
                    locality = entry.getValue();
                    isZaragozaBarrio = true;
                }
            }
        }

        // 5. Invertir el orden "APELLIDO, NOMBRE DE LA VIA" -> "NOMBRE DE LA VIA APELLIDO"
        if (cleanAddress.contains(",")) {
            String[] parts = cleanAddress.split(",", 2);
            cleanAddress = parts[1].trim() + " " + parts[0].trim();
        }

        // 6. Limpieza y expansión de abreviaturas habituales del callejero municipal
        cleanAddress = cleanAddress
                .replaceAll("\\bPZA\\.?\\b", "PLAZA ")
                .replaceAll("\\bPLZ\\.?\\b", "PLAZA ")
                .replaceAll("\\bAVDA\\.?\\b", "AVENIDA ")
                .replaceAll("\\bAV\\.?\\b", "AVENIDA ")
                .replaceAll("\\bCL\\.?\\b", "CALLE ")
                .replaceAll("\\bPS\\.?\\b", "PASEO ")
                .replaceAll("\\bGL\\.?\\b", "GLORIETA ")
                .replaceAll("\\bCTRA\\.?\\b", "CARRETERA ")
                .replaceAll("\\bCNO\\.?\\b", "CAMINO ")
                .replaceAll("\\bCN\\.?\\b", "CAMINO ")
                .replaceAll("\\bPG\\.?\\b", "POLIGONO ")
                .replaceAll("\\bPOL\\.?\\b", "POLIGONO ")
                .replaceAll("\\bPQ\\.?\\b", "PARQUE ")
                .replaceAll("\\bNTRA\\b\\.?\\s*", "NUESTRA ")
                .replaceAll("\\bSRA\\b\\.?\\s*", "SEÑORA ")
                .replaceAll("\\bFCO\\.?\\b", "FRANCISCO ")
                .replaceAll("\\bTTE\\.?\\b", "TENIENTE ")
                .replaceAll("\\bCNEL\\.?\\b", "CORONEL ");

        // 7. Detectar el tipo de vía y quitar del resto del texto todos los tipos de
        // vía para evitar duplicados como "AVENIDA ... CALLE ..."
        String streetType = "CALLE";
        for (String viaType : VIA_TYPES) {
            if (cleanAddress.matches(".*\\b" + viaType + "\\b.*")) {
                streetType = viaType;
                break;
            }
        }
        for (String viaType : VIA_TYPES) {
            cleanAddress = cleanAddress.replaceAll("\\b" + viaType + "\\b", "");
        }
        cleanAddress = cleanAddress.replaceAll("\\bCALLE\\b", "");
        cleanAddress = cleanAddress.replaceAll("\\s+", " ").trim();

        // 8. Construir el resultado: "TIPO_VIA NOMBRE" seguido de la localidad real,
        // en vez de anteponer la localidad al nombre de la calle (lo que generaba
        // queries sin sentido como "CALLE UTEBO 13KM LUGO ZARAGOZA").
        String result = (streetType + " " + cleanAddress).trim();

        result = result.replace("AVENIDA DE AMERICA", "AVENIDA AMERICA");
        if (result.contains("INDEPENDENCIA")) {
            result = "PASEO INDEPENDENCIA";
        }

        if (locality.isEmpty()) {
            result += ", ZARAGOZA";
        } else if (isZaragozaBarrio) {
            // Barrio rural/pedanía de Zaragoza capital: se añade como distrito y se
            // fuerza igualmente la ciudad "Zaragoza" para desambiguar frente a
            // topónimos iguales en otras provincias.
            result += locality.equals("ZARAGOZA") ? ", ZARAGOZA" : ", " + locality + ", ZARAGOZA";
        } else {
            // Localidad distinta dentro del área de cobertura de bomberos de Zaragoza
            // (comarca u otro municipio de la provincia): se geocodifica contra ESA
            // localidad, sin forzar "ZARAGOZA" delante, que es justo lo que hacía
            // fallar direcciones como "Lugo (Utebo)" o "Cl Francia (Zuera)".
            result += ", " + locality;
        }

        return result.replaceAll("\\s+", " ").trim();
    }

    public String getcoordinates(String address) {
        URI url = UriComponentsBuilder.fromHttpUrl("https://photon.komoot.io/api/")
                .queryParam("q", normalizeAddress(address))
                .queryParam("lat", 41.6488)
                .queryParam("lon", -0.8891)
                .queryParam("limit", "1")
                .build()
                .encode()
                .toUri();

        int waitTime = 1500;
        int maxRetries = 3;
        int attempt = 0;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "ZgzEmergencyMap/1.0 (juanfranvelilla@gmail.com)");
        headers.set("Accept-Language", "es");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Bucle para permitir reintentos si nos da un 429
        while (attempt < maxRetries) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                return response.getBody(); // Si va bien, devuelve y el finally se encarga de la pausa

            } catch (HttpClientErrorException.TooManyRequests e) {
                attempt++;
                System.err.println("Photon Limit Exceeded (429). Intento " + attempt + "/" + maxRetries + " para: " + address);

                if (attempt >= maxRetries) {
                    break; // Si ya hemos intentado 3 veces y sigue fallando, salimos del bucle
                }

                this.sleepSafe(waitTime);

            } finally {
                this.sleepSafe(waitTime);
            }
        }

        // Si llega a esta línea, es que falló todos los intentos
        System.err.println("Cancelado definitivamente tras " + maxRetries + " intentos: " + address);
        return "";
    }

    // Método auxiliar para detener la ejecucion determinados milisegundos
    private void sleepSafe(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
