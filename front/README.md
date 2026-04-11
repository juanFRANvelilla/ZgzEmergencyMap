# ZGZ Emergency Map — Frontend

## Requisitos

- **Node.js** 18 o superior  
- **npm** (incluido con Node)

## Instalación

```bash
cd front
npm install
```

## Configuración

- **URL del backend**: en `src/app/services/incident.service.ts`, propiedad `apiUrl`. Apunta al API Spring Boot (por ejemplo `http://localhost:8080` en local o la URL de tu despliegue). Ajusta este valor según el entorno.

## Comandos

| Acción | Comando |
|--------|---------|
| Servidor de desarrollo (por defecto `http://localhost:4200/`) | `npm start` o `ng serve` |
| Build de producción | `npm run build` |
| Build desarrollo con observación de cambios | `ng build --watch --configuration development` |

Los artefactos de producción quedan en **`dist/emergency-map-angular/browser/`** (salida del builder de aplicación de Angular).

## Despliegue (resumen)

Tras `npm run build`, sirve esa carpeta `browser` detrás de nginx u otro servidor estático; en este repo hay **`Dockerfile`** y **`nginx.conf`** de referencia.
