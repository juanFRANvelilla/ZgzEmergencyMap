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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeocodingServiceImpl implements GeocodingService {
    private final RestTemplate restTemplate;

    public GeocodingServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String normalizeAddress(String address) {
        if (address == null || address.isEmpty()) return "";

        String cleanAddress = address.toUpperCase();
        cleanAddress = cleanAddress.replace("\"", "").replace("\\", "");

        // 1. Borramos explícitamente la etiqueta (ZARAGOZA)
        cleanAddress = cleanAddress.replace("(ZARAGOZA)", "");

        // 2. Extraer el contenido de cualquier otro paréntesis que quede
        String bracketContent = "";
        Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(cleanAddress);
        if (matcher.find()) {
            bracketContent = matcher.group(1).trim();
            cleanAddress = cleanAddress.replaceAll("\\(.*?\\)", "");
        }

        // 3. Invertir el orden por la coma
        if (cleanAddress.contains(",")) {
            String[] parts = cleanAddress.split(",", 2);
            cleanAddress = parts[1].trim() + " " + parts[0].trim();
        }

        // 4. Limpieza de abreviaturas
        cleanAddress = cleanAddress
                .replaceAll("\\bPZA\\.?\\b", "PLAZA ")
                .replaceAll("\\bAVDA\\.?\\b", "AVENIDA ")
                .replaceAll("\\bAV\\.?\\b", "AVENIDA ")
                .replaceAll("\\bCL\\.?\\b", "CALLE ")
                .replaceAll("\\bPS\\.?\\b", "PASEO ")
                .replaceAll("\\bGL\\.?\\b", "GLORIETA ")
                .replaceAll("\\bNTRA\\b\\.?\\s*", "NUESTRA ")
                .replaceAll("\\bSRA\\b\\.?\\s*", "SEÑORA ")
                .replaceAll("\\bFCO\\.?\\b", "FRANCISCO ");

        // 5. Detectar y extraer el tipo de vía
        String streetType = "CALLE";
        if (cleanAddress.matches(".*\\bAVENIDA\\b.*")) {
            streetType = "AVENIDA";
        } else if (cleanAddress.matches(".*\\bPLAZA\\b.*")) {
            streetType = "PLAZA";
        } else if (cleanAddress.matches(".*\\bPASEO\\b.*")) {
            streetType = "PASEO";
        } else if (cleanAddress.matches(".*\\bGLORIETA\\b.*")) {
            streetType = "GLORIETA";
        }

        // Suprimimos todos los tipos de vía del resto del texto para evitar duplicados como "AVENIDA... CALLE..."
        cleanAddress = cleanAddress.replaceAll("\\bAVENIDA\\b", "")
                .replaceAll("\\bPLAZA\\b", "")
                .replaceAll("\\bPASEO\\b", "")
                .replaceAll("\\bGLORIETA\\b", "")
                .replaceAll("\\bCALLE\\b", "");

        // 6. Construir el resultado
        String result = streetType + " ";
        if (!bracketContent.isEmpty()) {
            result += bracketContent + " ";
        }
        result += cleanAddress;

        // 7. Limpiar espacios y añadir la ciudad final
        result = result.replaceAll("\\s+", " ").trim();

        // Caso excepcional para Avenida de América (para que la API la localice en Zaragoza capital)
        result = result.replace("AVENIDA DE AMERICA", "AVENIDA AMERICA");
        if(result.contains("INDEPENDENCIA")){
            result = "PASEO INDEPENDENCIA";
        } else if(result.contains("CASETAS CAMINO MOLINO")){
            result = "CALLE CAMINO MOLINO REY CASETAS";
        }
        return result + " ZARAGOZA";
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
