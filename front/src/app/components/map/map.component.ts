import { Component, OnInit, AfterViewInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import Map from 'ol/Map';
import View from 'ol/View';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import XYZ from 'ol/source/XYZ';
import { fromLonLat, toLonLat } from 'ol/proj';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import Feature from 'ol/Feature';
import Point from 'ol/geom/Point';
import Polygon from 'ol/geom/Polygon';
import Draw from 'ol/interaction/Draw';
import { Style, Icon, Stroke, Fill } from 'ol/style';
import Overlay from 'ol/Overlay';
import { IncidentService } from '../../services/incident.service';
import { IncidentModel, MarkerIcon } from '../../models/incident.model';
import { MAP_STYLES, MapStyleType } from '../../constants/map-styles';

interface PolygonIncident {
  incident: IncidentModel;
  visible: boolean;
}

const POLYGON_PANEL_LEFT = 60;
const POLYGON_PANEL_WIDTH = 340;
const MOBILE_BREAKPOINT = 900;

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {
  map!: Map;
  vectorLayer!: VectorLayer<VectorSource>;
  polygonLayer!: VectorLayer<VectorSource>;
  popup!: Overlay;
  popupIncident: IncidentModel | null = null;

  drawPolygon: Draw | null = null;
  isPolygonMode: boolean = false;
  loadingPolygonIncidents: boolean = false;
  polygonError: string = '';
  polygonIncidents: PolygonIncident[] = [];
  private polygonSubscription?: Subscription;
  private suppressMapClick: boolean = false;
  
  fullIncidentList: IncidentModel[] = [];
  filteredIncidents: IncidentModel[] = [];
  currentIncidents: IncidentModel[] = [];
  
  selectedDate: string = '';
  maxDate: string = '';
  showOnlyOpen: boolean = false;
  currentMapStyle: 'OSM' | 'LIGHT' | 'DARK' | 'SATELLITE' = 'OSM';
  showLayerMenu: boolean = false;
  
  showMap: boolean = true;
  showCheckbox: boolean = false;
  noIncidentMessage: string = '';
  showAttribution: boolean = false;

  toggleAttribution(): void {
    this.showAttribution = !this.showAttribution;
  }

  constructor(private incidentService: IncidentService) {}

  ngOnInit(): void {
    this.setTodayDate();
  }

  ngAfterViewInit(): void {
    // Esperar un tick para asegurar que el DOM esté listo
    setTimeout(() => {
      this.initMap();
      this.loadTodayIncidents();
    }, 0);

    // Agregar listener para resize
    window.addEventListener('resize', () => {
      if (this.map) {
        setTimeout(() => {
          this.map.updateSize();
        }, 200);
      }
    });
  }

  ngOnDestroy(): void {
    this.polygonSubscription?.unsubscribe();
    if (this.map) {
      this.map.setTarget(undefined);
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.drawPolygon) {
      this.stopDrawPolygon();
    }
  }

  setTodayDate(): void {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    this.selectedDate = `${year}-${month}-${day}`;
    this.maxDate = this.selectedDate;
  }

  initMap(): void {
    console.log('Inicializando mapa...');
    
    // Verificar que el elemento existe
    const mapElement = document.getElementById('map');
    if (!mapElement) {
      console.error('Elemento #map no encontrado en el DOM');
      return;
    }
    
    // Crear popup overlay
    const popupElement = document.getElementById('popup');
    if (popupElement) {
      this.popup = new Overlay({
        element: popupElement,
        positioning: 'bottom-center',
        stopEvent: false,
        offset: [0, -10]
      });
    }

    // Crear capa vectorial para los marcadores
    this.vectorLayer = new VectorLayer({
      source: new VectorSource()
    });

    // Capa para el polígono dibujado por el usuario (por debajo de los marcadores)
    this.polygonLayer = new VectorLayer({
      source: new VectorSource(),
      style: new Style({
        stroke: new Stroke({ color: 'rgba(0, 145, 194, 1)', width: 2 }),
        fill: new Fill({ color: 'rgba(0, 145, 194, 0.12)' })
      })
    });

    // Crear el mapa
    this.map = new Map({
      target: 'map',
      layers: [
        new TileLayer({
          source: new OSM({
            attributions: []
          })
        }),
        this.polygonLayer,
        this.vectorLayer
      ],
      view: new View({
        center: fromLonLat([-0.8966871819188688, 41.645268810703485]),
        zoom: 12
      }),
      overlays: this.popup ? [this.popup] : [],
      controls: []
    });
    
    // Forzar renderizado del mapa
    this.map.updateSize();
    
    console.log('Mapa inicializado correctamente', {
      target: this.map.getTarget(),
      size: this.map.getSize()
    });

    // Agregar evento de click para mostrar popup
    this.map.on('click', (event) => {
      // Contraer menú de capas al pulsar en el mapa
      this.showLayerMenu = false;

      if (this.drawPolygon || this.suppressMapClick) {
        return;
      }

      const feature = this.map.forEachFeatureAtPixel(event.pixel, (feat) => feat, {
        layerFilter: (layer) => layer === this.vectorLayer
      });
      if (feature && this.popup) {
        const coordinates = (feature.getGeometry() as Point).getCoordinates();
        const incident = feature.get('incident') as IncidentModel;
        this.showPopup(coordinates, incident);
        
        // Centrar vista en el incidente clickado
        this.map.getView().animate({
          center: coordinates,
          duration: 500
        });
      } else if (this.popup) {
        this.hidePopup();
      }
    });

    // Cambiar cursor cuando pasa sobre un marcador
    this.map.on('pointermove', (event) => {
      if (this.drawPolygon) {
        this.map.getTargetElement().style.cursor = 'crosshair';
        return;
      }
      const pixel = this.map.getEventPixel(event.originalEvent);
      const hit = this.map.hasFeatureAtPixel(pixel, {
        layerFilter: (layer) => layer === this.vectorLayer
      });
      this.map.getTargetElement().style.cursor = hit ? 'pointer' : '';
    });
  }

  loadTodayIncidents(): void {
    this.incidentService.getTodayIncidents().subscribe({
      next: (incidents) => {
        this.handleIncidentsLoaded(incidents);
      },
      error: (error) => {
        console.error('Error loading today incidents:', error);
        this.handleNoIncidents();
      }
    });
  }

  onDateChange(): void {
    this.hidePopup();
    this.showLayerMenu = false;
    if (this.isPolygonMode) {
      this.clearPolygon(false);
    }

    if (this.isCurrentDate(this.selectedDate)) {
      this.loadTodayIncidents();
    } else {
      this.showOnlyOpen = false;
      this.loadIncidentsByDate(this.selectedDate);
    }
  }

  loadIncidentsByDate(date: string): void {
    this.incidentService.getIncidentsByDate(date).subscribe({
      next: (incidents) => {
        this.handleIncidentsLoaded(incidents);
      },
      error: (error) => {
        console.error('Error loading incidents by date:', error);
        this.handleNoIncidents();
      }
    });
  }

  handleIncidentsLoaded(incidents: IncidentModel[]): void {
    console.log('Incidentes cargados:', incidents.length);
    this.fullIncidentList = incidents;
    
    if (incidents.length === 0) {
      this.handleNoIncidents();
      return;
    }

    // Siempre mostrar el mapa
    this.showMap = true;
    
    // Filtrar incidentes abiertos
    this.filteredIncidents = incidents.filter(i => i.status === 'OPEN');
    this.showCheckbox = this.filteredIncidents.length > 0;
    
    // Mostrar todos los incidentes o solo los abiertos según el checkbox
    this.currentIncidents = this.showOnlyOpen ? this.filteredIncidents : this.fullIncidentList;
    console.log('Actualizando mapa con', this.currentIncidents.length, 'incidentes');
    this.updateMap(true);
  }

  handleNoIncidents(): void {
    // No ocultar el mapa, solo limpiar los marcadores
    this.showMap = true;
    this.showCheckbox = false;
    this.fullIncidentList = [];
    this.filteredIncidents = [];
    this.currentIncidents = [];
    
    // Limpiar el mapa sin destruirlo
    if (this.vectorLayer && this.vectorLayer.getSource()) {
      this.vectorLayer.getSource()?.clear();
    }
    
    const date = new Date(this.selectedDate);
    const formattedDate = date.toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
    this.noIncidentMessage = `Vaya!! no hay incidencias disponibles para el ${formattedDate}`;
  }

  onToggleOpenIncidents(): void {
    this.hidePopup();
    if (this.isPolygonMode) {
      this.clearPolygon(false);
    }
    this.currentIncidents = this.showOnlyOpen ? this.filteredIncidents : this.fullIncidentList;
    this.updateMap(true);
  }

  toggleDrawPolygon(): void {
    if (this.drawPolygon) {
      this.stopDrawPolygon();
      return;
    }

    this.hidePopup();
    this.showLayerMenu = false;

    const polygonSource = this.polygonLayer.getSource()!;
    this.drawPolygon = new Draw({
      source: polygonSource,
      type: 'Polygon'
    });

    // Solo se permite un polígono: al empezar uno nuevo se borra el anterior
    this.drawPolygon.on('drawstart', () => polygonSource.clear());
    this.drawPolygon.on('drawend', (event) => {
      const polygon = event.feature.getGeometry() as Polygon;
      this.stopDrawPolygon();
      // El click que cierra el polígono no debe abrir el popup de un marcador
      this.suppressMapClick = true;
      setTimeout(() => this.suppressMapClick = false, 300);
      this.searchIncidentsInPolygon(polygon);
    });

    this.map.addInteraction(this.drawPolygon);
  }

  stopDrawPolygon(): void {
    if (this.drawPolygon) {
      this.map.removeInteraction(this.drawPolygon);
      this.drawPolygon = null;
      this.map.getTargetElement().style.cursor = '';
    }
  }

  searchIncidentsInPolygon(polygon: Polygon): void {
    this.hidePopup();
    this.isPolygonMode = true;
    this.loadingPolygonIncidents = true;
    this.polygonError = '';
    this.polygonIncidents = [];
    this.updateMap(false);
    this.fitToPolygon(polygon);

    const coordinates = polygon.getCoordinates()[0].map(coordinate => toLonLat(coordinate));

    this.polygonSubscription?.unsubscribe();
    this.polygonSubscription = this.incidentService.getIncidentsByPolygon(coordinates).subscribe({
      next: (incidents) => {
        this.polygonIncidents = incidents.map(incident => ({ incident, visible: true }));
        this.loadingPolygonIncidents = false;
        this.updateMap(false);
      },
      error: (error) => {
        console.error('Error loading incidents by polygon:', error);
        this.loadingPolygonIncidents = false;
        this.polygonError = 'No se han podido obtener las incidencias de esta zona';
      }
    });
  }

  fitToPolygon(polygon: Polygon): void {
    const isMobile = window.innerWidth <= MOBILE_BREAKPOINT;
    // Dejar hueco para el panel de la lista (a la izquierda en escritorio, abajo en móvil)
    // y para la franja inferior de #map que queda fuera de pantalla bajo la cabecera
    const padding = isMobile
      ? [60, 30, Math.round(window.innerHeight * 0.4) + 150, 30]
      : [60, 60, 130, POLYGON_PANEL_LEFT + POLYGON_PANEL_WIDTH + 30];

    this.map.getView().fit(polygon.getExtent(), {
      padding,
      maxZoom: 17,
      duration: 400
    });
  }

  /** `redraw = false` cuando quien llama ya va a repintar el mapa con los incidentes del día. */
  clearPolygon(redraw: boolean = true): void {
    this.stopDrawPolygon();
    this.polygonSubscription?.unsubscribe();
    this.polygonLayer.getSource()?.clear();
    this.isPolygonMode = false;
    this.loadingPolygonIncidents = false;
    this.polygonError = '';
    this.polygonIncidents = [];
    this.hidePopup();
    if (redraw) {
      this.updateMap(true);
    }
  }

  get allPolygonIncidentsVisible(): boolean {
    return this.polygonIncidents.length > 0 && this.polygonIncidents.every(item => item.visible);
  }

  setAllPolygonIncidentsVisible(visible: boolean): void {
    this.polygonIncidents.forEach(item => item.visible = visible);
    if (!visible) {
      this.hidePopup();
    }
    this.updateMap(false);
  }

  setPolygonIncidentVisible(item: PolygonIncident, visible: boolean): void {
    item.visible = visible;
    if (!visible && this.popupIncident === item.incident) {
      this.hidePopup();
    }
    this.updateMap(false);
  }

  showPolygonIncidentInfo(item: PolygonIncident): void {
    if (!item.visible) {
      item.visible = true;
      this.updateMap(false);
    }

    const coordinates = fromLonLat([item.incident.longitude, item.incident.latitude]);
    this.showPopup(coordinates, item.incident);
    this.map.getView().animate({
      center: coordinates,
      duration: 500
    });
  }

  toggleLayerMenu(): void {
    this.showLayerMenu = !this.showLayerMenu;
  }

  onStyleChange(style: 'OSM' | 'LIGHT' | 'DARK' | 'SATELLITE'): void {
    this.currentMapStyle = style;
    this.showLayerMenu = false; // Cerrar el menú después de seleccionar
    this.updateMapStyle();
  }

  updateMapStyle(): void {
    const layers = this.map.getLayers().getArray();
    const tileLayer = layers[0] as TileLayer<any>;
    
    switch (this.currentMapStyle) {
      case 'OSM':
        tileLayer.setSource(new OSM({
          attributions: []
        }));
        break;
      case 'DARK':
        tileLayer.setSource(new XYZ({
          url: 'https://{a-c}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png',
        }));
        break;
      case 'LIGHT':
        tileLayer.setSource(new XYZ({
          url: 'https://{a-c}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png'
        }));
        break;
      case 'SATELLITE':
          tileLayer.setSource(new XYZ({
            url: 'https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
          }));
        break;
    }
    this.updateMap(false);
  }

  updateMap(fitView: boolean = true): void {
    const source = this.vectorLayer.getSource();
    if (!source) return;

    source.clear();

    // En modo polígono solo se pintan las incidencias de la zona marcadas como visibles
    const incidentsToRender = this.isPolygonMode
      ? this.polygonIncidents.filter(item => item.visible).map(item => item.incident)
      : this.currentIncidents;

    incidentsToRender.forEach(incident => {
      const feature = new Feature({
        geometry: new Point(fromLonLat([incident.longitude, incident.latitude]))
      });

      feature.set('incident', incident);
      
      const iconUrl = this.selectIcon(incident.markerIcon);
      feature.setStyle(new Style({
        image: new Icon({
          src: iconUrl,
          scale: 0.05,  // Iconos muy pequeños, como puntos
          anchor: [0.5, 0.5],  // Centrado
          anchorXUnits: 'fraction',
          anchorYUnits: 'fraction'
        })
      }));

      source.addFeature(feature);
    });

    // Ajustar vista para mostrar todos los marcadores (en modo polígono manda el zoom al polígono)
    if (fitView && !this.isPolygonMode && incidentsToRender.length > 0) {
      const extent = source.getExtent();
      this.map.getView().fit(extent, {
        padding: [50, 50, 50, 50],
        maxZoom: 12.8
      });
    }
  }

  selectIcon(markerIcon: MarkerIcon): string {
    const iconsLight: Record<MarkerIcon, string> = {
      DEFAULT: 'assets/markerIcons/defaultIcon.png',
      FIRE: 'assets/markerIcons/fireIcon.png',
      TREE: 'assets/markerIcons/treeIcon.png',
      TRAFFIC: 'assets/markerIcons/trafficIcon.png',
      ELEVATOR: 'assets/markerIcons/elevatorIcon.png',
      CONSTRUCTION: 'assets/markerIcons/buildIcon.png',
      ANIMALS: 'assets/markerIcons/animalIcon.png',
      DANGEROUSPRODUCT: 'assets/markerIcons/dangerIcon.png',
      BLOCKED: 'assets/markerIcons/blockedIcon.png',
      WATERDRAINAGE: 'assets/markerIcons/waterIcon.png'
    };

    const iconsDark: Record<MarkerIcon, string> = {
      DEFAULT: 'assets/markerIcons/defaultIcon.png',
      FIRE: 'assets/markerIcons/fireIcon.png',
      TREE: 'assets/markerIcons/treeIcon.png',
      TRAFFIC: 'assets/markerIcons/trafficIconDark.png',
      ELEVATOR: 'assets/markerIcons/elevatorIconDark.png',
      CONSTRUCTION: 'assets/markerIcons/buildIcon.png',
      ANIMALS: 'assets/markerIcons/animalIconDark.png',
      DANGEROUSPRODUCT: 'assets/markerIcons/dangerIcon.png',
      BLOCKED: 'assets/markerIcons/blockedIcon.png',
      WATERDRAINAGE: 'assets/markerIcons/waterIconDark.png'
    };

    const icons = this.currentMapStyle === 'DARK' ? iconsDark : iconsLight;
    return icons[markerIcon] || icons.DEFAULT;
  }

  showPopup(coordinates: number[], incident: IncidentModel): void {
    if (!this.popup) return;

    const popupContent = document.getElementById('popup-content');
    if (popupContent) {
      popupContent.innerHTML = `
        <div class="incident-info">
          <h4>${incident.incidentType}</h4>
          <p><strong>Dirección:</strong> ${incident.address}</p>
          ${this.isPolygonMode ? `<p><strong>Fecha:</strong> ${this.formatIncidentDate(incident.date)}</p>` : ''}
          <p><strong>Inicio:</strong> ${incident.time}</p>
          ${incident.status === 'CLOSED' && incident.duration ? `<p><strong>Duración:</strong> ${incident.duration}</p>` : ''}
          ${incident.resources && incident.resources.length > 0 ? `
            <p><strong>Recursos:</strong></p>
            <ul>
              ${incident.resources.map(r => `<li>${r}</li>`).join('')}
            </ul>
          ` : ''}
        </div>
      `;
    }

    this.popupIncident = incident;
    this.popup.setPosition(coordinates);
  }

  hidePopup(): void {
    this.popupIncident = null;
    if (this.popup) {
      this.popup.setPosition(undefined);
    }
  }

  /** 'yyyy-MM-dd' -> 'dd/MM/yyyy' */
  formatIncidentDate(date: string): string {
    const [year, month, day] = date.split('-');
    return `${day}/${month}/${year}`;
  }

  isCurrentDate(dateString: string): boolean {
    const inputDate = new Date(dateString);
    const today = new Date();
    return (
      inputDate.getFullYear() === today.getFullYear() &&
      inputDate.getMonth() === today.getMonth() &&
      inputDate.getDate() === today.getDate()
    );
  }
}


