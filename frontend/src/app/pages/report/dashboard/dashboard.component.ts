import { Component, OnInit } from '@angular/core';
import { BasicMetricsDto } from '../../../models/basic-metrics-dto';
import { QuickDanceMetrics } from '../../../models/quick-dance-metrics';
import { QuickGeographyMetrics } from '../../../models/quick-geography-metrics';
import { ReportService } from '../../../services/report.service';
import { forkJoin } from 'rxjs/internal/observable/forkJoin';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  basicMetrics: BasicMetricsDto | null = null;
  quickDanceMetrics: QuickDanceMetrics | null = null;
  quickGeographyMetrics: QuickGeographyMetrics | null = null;
  
  loading = false;
  error: string | null = null;

  constructor(private reportsService: ReportService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.error = null;

    forkJoin({
      basicMetrics: this.reportsService.getBasicMetrics(),
      quickDance: this.reportsService.getQuickDanceMetrics(),
      quickGeography: this.reportsService.getQuickGeographyMetrics()
    }).subscribe({
      next: (data) => {
        this.basicMetrics = data.basicMetrics;
        this.quickDanceMetrics = data.quickDance;
        this.quickGeographyMetrics = data.quickGeography;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'No se pudieron cargar los datos del dashboard';
        this.loading = false;
        console.error('Error loading dashboard data:', err);
      }
    });
  }
}
