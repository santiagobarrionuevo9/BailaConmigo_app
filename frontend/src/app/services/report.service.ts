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
import { EventRatingsReport } from '../models/event-ratings-report';
import { QuickRatingMetrics } from '../models/quick-rating-metrics';
import { RatingDashboard } from '../models/rating-dashboard';
import { DanceStyleSatisfaction } from '../models/dance-style-satisfaction';
import { EventTypeSatisfaction } from '../models/event-type-satisfaction';
import { ProfileRatingsReport } from '../models/profile-ratings-report';
import { FinancialReportDto } from '../models/financial-report-dto';
import { DashboardSummaryDto } from '../models/dashboard-summary-dto';
import { RegistrationReportDto } from '../models/registration-report-dto';
import { EventDetailReportDto } from '../models/event-detail-report-dto';
import { EventGeneralReportDto } from '../models/event-general-report-dto';


@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly baseUrl = 'https://0f78-152-171-81-105.ngrok-free.app/api/reports'; // Ajusta según tu configuración

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
  
  

  // === MÉTRICAS RÁPIDAS ===
  
  getQuickDanceMetrics(): Observable<QuickDanceMetrics> {
    return this.http.get<QuickDanceMetrics>(`${this.baseUrl}/quick/dance`);
  }

  getQuickGeographyMetrics(): Observable<QuickGeographyMetrics> {
    return this.http.get<QuickGeographyMetrics>(`${this.baseUrl}/quick/geography`);
  }

  // Reporte general de ratings de eventos
  getEventRatingsReport(): Observable<EventRatingsReport> {
    return this.http.get<EventRatingsReport>(`${this.baseUrl}/ratings/events`);
  }

  // Reporte general de ratings de perfiles
  getProfileRatingsReport(): Observable<ProfileRatingsReport> {
    return this.http.get<ProfileRatingsReport>(`${this.baseUrl}/ratings/profiles`);
  }

  // Satisfacción por tipo de evento
  getEventTypeSatisfactionReport(): Observable<EventTypeSatisfaction[]> {
    return this.http.get<EventTypeSatisfaction[]>(`${this.baseUrl}/satisfaction/event-types`);
  }

  // Satisfacción por estilo de baile
  getDanceStyleSatisfactionReport(): Observable<DanceStyleSatisfaction[]> {
    return this.http.get<DanceStyleSatisfaction[]>(`${this.baseUrl}/satisfaction/dance-styles`);
  }

  // Dashboard completo con métricas de rating
  getCompleteRatingDashboard(): Observable<RatingDashboard> {
    return this.http.get<RatingDashboard>(`${this.baseUrl}/dashboard/ratings`);
  }

  // Métricas rápidas de ratings
  getQuickRatingMetrics(): Observable<QuickRatingMetrics> {
    return this.http.get<QuickRatingMetrics>(`${this.baseUrl}/quick/ratings`);
  }

  /**
   * Obtiene un reporte general de todos los eventos e inscripciones
   */
  getGeneralReport(): Observable<EventGeneralReportDto> {
    return this.http.get<EventGeneralReportDto>(`${this.baseUrl}/general`);
  }

  /**
   * Obtiene un reporte detallado de un evento específico
   */
  getEventDetailReport(eventId: number): Observable<EventDetailReportDto> {
    return this.http.get<EventDetailReportDto>(`${this.baseUrl}/events/${eventId}`);
  }

  /**
   * Obtiene un reporte de inscripciones por período
   */
  getRegistrationReport(startDate: string, endDate: string): Observable<RegistrationReportDto> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<RegistrationReportDto>(`${this.baseUrl}/registrations`, { params });
  }


  /**
   * Obtiene un reporte financiero por período
   */
  getFinancialReport(startDate: string, endDate: string): Observable<FinancialReportDto> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<FinancialReportDto>(`${this.baseUrl}/financial`, { params });
  }

  /**
   * Obtiene un resumen rápido para dashboard
   */
  getDashboardSummary(): Observable<DashboardSummaryDto> {
    return this.http.get<DashboardSummaryDto>(`${this.baseUrl}/summary`);
  }

}