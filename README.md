# ZGZ Emergency Map

## Acerca de este proyecto

**ZGZ Emergency Map** es una aplicación web que muestra en un mapa interactivo las **incidencias atendidas por el cuerpo de bomberos en Zaragoza**, usando datos públicos del Ayuntamiento. El sistema está dividido en un **frontend** (SPA con mapa) y un **backend** (API y persistencia) que trabajan juntos: el backend consulta periódicamente la fuente oficial, enriquece y guarda la información, y el frontend la consume para visualizarla.

### Resumen del flujo de datos

1. **Fuente externa**  
   El backend llama al **servicio público de bomberos** del Ayuntamiento de Zaragoza (`https://www.zaragoza.es/sede/servicio/bomberos`), con distintos parámetros `tipo` en la query (por ejemplo **`20`** incidentes cerrados recientes, **`10`** abiertos en curso, **`21`** histórico del día anterior). Las respuestas son JSON que se adaptan al modelo interno.

2. **Lógica en el servidor**  
   Esas respuestas se convierten a entidades de dominio. Las **direcciones de texto** se geocodifican para obtener **latitud y longitud** (ver apartado siguiente), y todo se **persiste en PostgreSQL**. El backend distingue incidentes **en curso** y **finalizados**, y reconcilia el estado cuando un incidente deja de aparecer como abierto en la API pública pero no llega aún en el listado de cerrados: se evitan incidencias abiertas “huérfanas” en base de datos (`handleLostIncidents`).

3. **API de coordenadas (geocodificación)**  
   Las coordenadas no vienen del Ayuntamiento en el formato que usa el mapa: el servicio **`GeocodingServiceImpl`** llama a la API pública **[Photon](https://photon.komoot.io)** (`https://photon.komoot.io/api/`), un geocodificador basado en datos de OpenStreetMap. Se envía la dirección normalizada (con sesgo hacia Zaragoza mediante `lat` / `lon` en la petición) y se recibe una respuesta tipo **GeoJSON** con el punto; el flujo incluye **reintentos** si Photon responde `429` (límite de peticiones). Esa capa es independiente de la API de bomberos.

4. **Consumo por el frontend**  
   La aplicación Angular consume directamente el **propio backend** (`/getTodayIncident`, `/getIncidentByDate`, etc.) y recibe incidentes ya con coordenadas y metadatos para pintarlos en el mapa, con filtros por **fecha** y por **estado** para determinar si siguen en curso.

---

## Frontend (`front/`)

- **Stack**: **Angular** (componentes standalone), **OpenLayers** (mapas; capas base configurables, p. ej. estilos tipo CartoDB claro/oscuro u OpenStreetMap), **Bootstrap**, **TypeScript** y **RxJS**.
- **Mapa**: marcadores según **tipo de siniestro** (fuego, tráfico, ascensores, animales, construcción, drenaje, etc.), popups con detalle y vista adaptable a móvil.
- **Comunicación**: `IncidentService` apunta a la **URL base del backend** (configurable en código; en despliegue puede usarse un host como API en la nube). Endpoints habituales: `GET /getTodayIncident`, `GET /getIncidentByDate?date=YYYY-MM-DD`.
- **Estáticos**: iconos e imágenes en `public/assets/`; en producción el bundle se suele servir con **nginx** (`front/nginx.conf`).

---

## Backend (`back/`)

- **Stack**: **Java 17**, **Spring Boot 3** (web, JPA, Actuator), **PostgreSQL**, **Lombok**, cliente HTTP con **`RestTemplate`**.
- **Integración Zaragoza**: `IncidentsZgzDataServiceImpl` descarga el JSON de bomberos; `JsonConverterServiceImpl` lo mapea a entidades `Incident`, recursos asociados y selección de icono de marcador.
- **Persistencia**: `IncidentServiceImpl` y repositorios JPA; cierre masivo o selectivo de abiertos según la última respuesta de la API pública.
- **Tareas programadas**: `ScheduledTask` lanza periódicamente la recarga de incidencias del día (intervalo definido en la clase).
- **API REST**: `IncidentController` y otros controladores (p. ej. prueba de geocodificación, direcciones no resueltas). **CORS** en `WebConfig` para orígenes de desarrollo y producción.

### Testing

- **JUnit 5**, **Spring Boot Test** (carga de contexto) y **Mockito** en `JsonConverterServiceTest` (parseo de JSON de ejemplo y colaboración con geocodificación y persistencia simulada).
- Ejecución: `./mvnw test` o `mvn test` desde `back/`.

---

## Estructura del repositorio

```
ZgzEmergencyMap/
├── back/          # Spring Boot, API y persistencia
├── front/         # Angular + OpenLayers
└── k8s/, Docker, etc. (despliegue)
```

**Instalación y configuración** (dependencias, variables de entorno, arranque en local): **`front/README.md`** y **`back/README.md`**.
