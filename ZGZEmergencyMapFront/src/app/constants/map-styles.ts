// Estilos de mapa para OpenLayers usando OSM (OpenStreetMap)
// OpenLayers no usa estilos de Google Maps, pero podemos configurar diferentes tiles

export const MAP_STYLES = {
  LIGHT: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png',
  DARK: 'https://{a-c}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png',
  // Alternativa para modo claro con CartoDB
  LIGHT_CARTO: 'https://{a-c}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png'
};

export type MapStyleType = 'LIGHT' | 'DARK';


