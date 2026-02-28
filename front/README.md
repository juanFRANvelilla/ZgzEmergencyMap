# ZGZ Emergency Map - Angular + OpenLayers

Esta es una aplicación Angular moderna que muestra incidentes de emergencia en un mapa interactivo usando OpenLayers.

## Características

- 🗺️ **Mapa interactivo** con OpenLayers
- 📍 **Marcadores personalizados** por tipo de incidente
- 🎨 **Dos estilos de mapa**: claro y oscuro
- 📅 **Filtro por fecha**: visualiza incidentes de cualquier día
- 🔄 **Filtro de incidentes activos**: muestra solo los incidentes en curso
- 📱 **Diseño responsive**: funciona en móviles, tablets y escritorio
- 💬 **Popups informativos**: detalles completos de cada incidente

## Tecnologías utilizadas

- **Angular 19** (standalone components)
- **OpenLayers** para mapas interactivos
- **Bootstrap 5** para estilos base
- **TypeScript** para type safety
- **RxJS** para programación reactiva

## Estructura del proyecto

```
src/
├── app/
│   ├── components/
│   │   └── map/                    # Componente principal del mapa
│   ├── models/
│   │   └── incident.model.ts       # Modelos de datos
│   ├── services/
│   │   └── incident.service.ts     # Servicio para consumir API
│   ├── constants/
│   │   └── map-styles.ts           # Configuración de estilos de mapa
│   └── app.component.ts            # Componente raíz
├── styles.css                      # Estilos globales
└── index.html                      # HTML principal

public/
└── assets/
    ├── markerIcons/                # Iconos de marcadores
    └── img/                        # Imágenes de la UI
```

## Instalación

1. Asegúrate de tener Node.js instalado (versión 18 o superior)
2. Las dependencias ya están instaladas, pero si necesitas reinstalarlas:

```bash
npm install
```

## Comandos disponibles

### Servidor de desarrollo

```bash
npm start
```

o

```bash
ng serve
```

Abre tu navegador en `http://localhost:4200/`

### Compilar para producción

```bash
npm run build
```

Los archivos compilados estarán en `dist/emergency-map-angular/`

### Compilar y observar cambios

```bash
ng build --watch --configuration development
```

## API Backend

La aplicación consume datos del backend desplegado en:
`https://zgzemergencymapback-europe.onrender.com`

### Endpoints utilizados:

- `GET /getTodayIncident` - Obtiene incidentes del día actual
- `GET /getIncidentByDate?date=YYYY-MM-DD` - Obtiene incidentes de una fecha específica

## Funcionalidades

### 1. Visualización de Incidentes
- Los incidentes se muestran como marcadores en el mapa
- Cada tipo de incidente tiene un icono personalizado
- Al hacer clic en un marcador, se muestra información detallada

### 2. Filtros
- **Por fecha**: Selector de fecha para ver incidentes históricos
- **Solo abiertos**: Toggle para mostrar únicamente incidentes en curso

### 3. Estilos de Mapa
- **Modo claro**: Estilo CartoDB Light
- **Modo oscuro**: Estilo CartoDB Dark

### 4. Tipos de Incidentes
- 🔥 Fuego (FIRE)
- 🌳 Árboles (TREE)
- 🚗 Tráfico (TRAFFIC)
- 🏢 Ascensores (ELEVATOR)
- 🏗️ Construcción (CONSTRUCTION)
- 🐕 Animales (ANIMALS)
- ☠️ Productos peligrosos (DANGEROUSPRODUCT)
- 🚧 Bloqueado (BLOCKED)
- 💧 Drenaje de agua (WATERDRAINAGE)
- 📍 Por defecto (DEFAULT)

## Comparación con el proyecto anterior

Esta aplicación **replica completamente** la funcionalidad del proyecto en `oldProject/juanFranciscoPerezWeb/zgzEmergencyMap`, pero usando:

✅ **Angular** en lugar de JavaScript vanilla
✅ **OpenLayers** en lugar de Google Maps
✅ **TypeScript** para mejor mantenibilidad
✅ **Componentes standalone** (Angular moderno)
✅ **Arquitectura escalable** con servicios y modelos

## Mejoras sobre el proyecto original

1. **Mejor organización del código** con arquitectura Angular
2. **Type safety** con TypeScript
3. **Mantenibilidad** con separación de responsabilidades
4. **Reactividad** con RxJS observables
5. **Sin dependencias de Google Maps** (OpenLayers es open source)

## Despliegue

Para desplegar la aplicación:

1. Compila el proyecto:
```bash
npm run build
```

2. Sube el contenido de `dist/emergency-map-angular/browser/` a tu servidor web o servicio de hosting (Netlify, Vercel, Firebase Hosting, etc.)

## Desarrollo

Para añadir nuevas funcionalidades:

1. **Nuevos componentes**: `ng generate component components/nombre`
2. **Nuevos servicios**: `ng generate service services/nombre`
3. **Nuevos modelos**: Crea archivos en `src/app/models/`

## Autor

Juan Francisco Pérez

## Licencia

Este proyecto es de uso educativo y personal.
