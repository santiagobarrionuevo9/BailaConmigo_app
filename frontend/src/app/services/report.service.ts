import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DashboardStats } from '../models/dashboard-stats';
import { Observable } from 'rxjs/internal/Observable';
import { BasicMetricsDto } from '../models/basic-metrics-dto';
import { MatchingReportDto } from '../models/matching-report-dto';
import { DanceDiversityReportDto } from '../models/dance-diversity-report-dto';
import { DanceStyleDistributionDto } from '../models/dance-style-distribution-dto';
import { StyleCombinationDto } from '../models/style-combination-dto';
import { GeographicReportDto } from '../models/geographic-report-dto';
import { QuickDanceMetrics } from '../models/quick-dance-metrics';
import { QuickGeographyMetrics } from '../models/quick-geography-metrics';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly baseUrl = 'http://localhost:8080/api/reports'; // Ajusta según tu configuración

  constructor(private http: HttpClient) {}

  // === DASHBOARD Y MÉTRICAS GENERALES ===
  
  getUserDashboard(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.baseUrl}/dashboard`);
  }

  getCompleteDashboard(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.baseUrl}/dashboard/complete`);
  }

  getBasicMetrics(): Observable<BasicMetricsDto> {
    return this.http.get<BasicMetricsDto>(`${this.baseUrl}/metrics`);
  }

  // === ACTIVIDAD DE MATCHING ===
  
  getMatchingActivityReport(): Observable<MatchingReportDto> {
    return this.http.get<MatchingReportDto>(`${this.baseUrl}/activity`);
  }

  getMatchingReportByDateRange(startDate: string, endDate: string): Observable<MatchingReportDto> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<MatchingReportDto>(`${this.baseUrl}/activity/range`, { params });
  }

  // === DIVERSIDAD DE BAILE ===
  
  getDanceDiversityReport(): Observable<DanceDiversityReportDto> {
    return this.http.get<DanceDiversityReportDto>(`${this.baseUrl}/dance/diversity`);
  }

  getDanceStyles(): Observable<DanceStyleDistributionDto[]> {
    return this.http.get<DanceStyleDistributionDto[]>(`${this.baseUrl}/dance/styles`);
  }

  getStyleCombinations(limit: number = 10): Observable<StyleCombinationDto[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<StyleCombinationDto[]>(`${this.baseUrl}/dance/combinations`, { params });
  }

  // === GEOGRAFÍA ===
  
  getGeographicReport(): Observable<GeographicReportDto> {
    // Asumiendo que existe este endpoint basado en el patrón
    return this.http.get<GeographicReportDto>(`${this.baseUrl}/geography`);
  }

  // === MÉTRICAS RÁPIDAS ===
  
  getQuickDanceMetrics(): Observable<QuickDanceMetrics> {
    return this.http.get<QuickDanceMetrics>(`${this.baseUrl}/quick/dance`);
  }

  getQuickGeographyMetrics(): Observable<QuickGeographyMetrics> {
    return this.http.get<QuickGeographyMetrics>(`${this.baseUrl}/quick/geography`);
  }
}