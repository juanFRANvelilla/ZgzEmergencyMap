import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Incident, IncidentListResponse, IncidentModel } from '../models/incident.model';

@Injectable({
  providedIn: 'root'
})
export class IncidentService {
  // Backend URL configuration from env.js or default
  private apiUrl = (window as any).__env?.API_URL || 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  getTodayIncidents(): Observable<IncidentModel[]> {
    return this.http.get<IncidentListResponse>(`${this.apiUrl}/getTodayIncident`).pipe(
      map(response => response.incidentList.map(incident => new IncidentModel(incident)))
    );
  }

  getIncidentsByDate(date: string): Observable<IncidentModel[]> {
    const formattedDate = encodeURIComponent(date);
    return this.http.get<IncidentListResponse>(`${this.apiUrl}/getIncidentByDate?date=${formattedDate}`).pipe(
      map(response => response.incidentList.map(incident => new IncidentModel(incident)))
    );
  }
}

