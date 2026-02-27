export interface Resource {
  id: number;
  name: string;
}

export interface IncidentResource {
  id: number;
  resource: Resource;
}

export interface Incident {
  id: number;
  date: string;
  time: string;
  status: 'OPEN' | 'CLOSED';
  incidentType: string;
  markerIcon: MarkerIcon;
  address: string;
  duration: string;
  latitude: number;
  longitude: number;
  incidentResources: IncidentResource[];
}

export interface IncidentListResponse {
  incidentList: Incident[];
}

export type MarkerIcon = 
  | 'DEFAULT'
  | 'FIRE'
  | 'TREE'
  | 'TRAFFIC'
  | 'ELEVATOR'
  | 'CONSTRUCTION'
  | 'ANIMALS'
  | 'DANGEROUSPRODUCT'
  | 'BLOCKED'
  | 'WATERDRAINAGE';

export class IncidentModel {
  id: number;
  date: string;
  time: string;
  status: 'OPEN' | 'CLOSED';
  incidentType: string;
  markerIcon: MarkerIcon;
  address: string;
  duration: string;
  latitude: number;
  longitude: number;
  resources: string[];

  constructor(incident: Incident) {
    this.id = incident.id;
    this.date = incident.date;
    this.time = incident.time;
    this.status = incident.status;
    this.incidentType = incident.incidentType;
    this.markerIcon = incident.markerIcon;
    this.address = incident.address;
    this.duration = incident.duration;
    this.latitude = incident.latitude;
    this.longitude = incident.longitude;
    this.resources = incident.incidentResources.map(ir => ir.resource.name);
  }
}


