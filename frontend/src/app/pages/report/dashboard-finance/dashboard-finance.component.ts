import { Component, OnInit } from '@angular/core';
import { DashboardSummaryDto } from '../../../models/dashboard-summary-dto';
import { ReportService } from '../../../services/report.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { EventGeneralReportDto } from '../../../models/event-general-report-dto';
import { EventType } from '../../../models/event-type';

@Component({
  selector: 'app-dashboard-finance',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-finance.component.html',
  styleUrl: './dashboard-finance.component.css'
})
export class DashboardFinanceComponent implements OnInit {
  reportData?: EventGeneralReportDto;
  loading = false;
  errorMessage = '';

  constructor(private reportService: ReportService) {}

  ngOnInit() {
    this.loadReport();
  }

  loadReport() {
    this.loading = true;
    this.errorMessage = '';

    this.reportService.getGeneralReport().subscribe({
      next: (data) => {
        this.reportData = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading general report:', error);
        this.errorMessage = 'Error al cargar el reporte general';
        this.loading = false;
      }
    });
  }

  getEventTypeEntries(): Array<{ key: string, value: number }> {
    if (!this.reportData?.eventsByType) return [];
    return Object.entries(this.reportData.eventsByType).map(([key, value]) => ({
      key,
      value
    }));
  }

  getDanceStyleEntries(): Array<{ key: string, value: number }> {
    if (!this.reportData?.danceStylePopularity) return [];
    return Object.entries(this.reportData.danceStylePopularity).map(([key, value]) => ({
      key,
      value
    }));
  }

  getPercentage(value: number, total: number): number {
    if (total === 0) return 0;
    return (value / total) * 100;
  }

  getEventTypeLabel(type: string): string {
    switch (type) {
      case EventType.CLASE: return 'Clase';
      case EventType.COMPETENCIA: return 'Competencia';
      case EventType.FESTIVAL: return 'Festival';
      case EventType.SOCIAL: return 'Social';
      default: return type;
    }
  }

  

  getTotalDanceStyles(): number {
    if (!this.reportData?.danceStylePopularity) return 0;
    return Object.values(this.reportData.danceStylePopularity).reduce((sum, count) => sum + count, 0);
  }
}