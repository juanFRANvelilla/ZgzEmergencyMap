import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Map from 'ol/Map';
import View from 'ol/View';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import XYZ from 'ol/source/XYZ';
import { fromLonLat } from 'ol/proj';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import Feature from 'ol/Feature';
import Point from 'ol/geom/Point';
import { Style, Icon } from 'ol/style';
import Overlay from 'ol/Overlay';
import { IncidentService } from '../../services/incident.service';
import { IncidentModel, MarkerIcon } from '../../models/incident.model';
import { MAP_STYLES, MapStyleType } from '../../constants/map-styles';

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
  popup!: Overlay;
  
  fullIncidentList: IncidentModel[] = [];
  filteredIncidents: IncidentModel[] = [];
  currentIncidents: IncidentModel[] = [];
  
  selectedDate: string = '';
  showOnlyOpen: boolean = false;
  currentMapStyle: 'OSM' | 'LIGHT' | 'DARK' | 'SATELLITE' = 'OSM';
  showLayerMenu: boolean = false;
  
  showMap: boolean = true;
  showCheckbox: boolean = false;
  noIncidentMessage: string = '';

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
    if (this.map) {
      this.map.setTarget(undefined);
    }
  }

  setTodayDate(): void {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    this.selectedDate = `${year}-${month}-${day}`;
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

    // Crear el mapa
    this.map = new Map({
      target: 'map',
      layers: [
        new TileLayer({
          source: new OSM({
            attributions: []
          })
        }),
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
      const feature = this.map.forEachFeatureAtPixel(event.pixel, (feat) => feat);
      if (feature && this.popup) {
        const coordinates = (feature.getGeometry() as Point).getCoordinates();
        const incident = feature.get('incident') as IncidentModel;
        this.showPopup(coordinates, incident);
      } else if (this.popup) {
        this.hidePopup();
      }
    });

    // Cambiar cursor cuando pasa sobre un marcador
    this.map.on('pointermove', (event) => {
      const pixel = this.map.getEventPixel(event.originalEvent);
      const hit = this.map.hasFeatureAtPixel(pixel);
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
    this.updateMap();
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
    this.currentIncidents = this.showOnlyOpen ? this.filteredIncidents : this.fullIncidentList;
    this.updateMap();
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
    this.updateMap();
  }

  updateMap(): void {
    const source = this.vectorLayer.getSource();
    if (!source) return;

    source.clear();

    this.currentIncidents.forEach(incident => {
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

    // Ajustar vista para mostrar todos los marcadores
    if (this.currentIncidents.length > 0) {
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

    this.popup.setPosition(coordinates);
  }

  hidePopup(): void {
    if (this.popup) {
      this.popup.setPosition(undefined);
    }
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


