import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { EventRatingsReport } from '../../../models/event-ratings-report';
import { ProfileRatingsReport } from '../../../models/profile-ratings-report';
import { EventTypeSatisfaction } from '../../../models/event-type-satisfaction';
import { DanceStyleSatisfaction } from '../../../models/dance-style-satisfaction';
import { QuickRatingMetrics } from '../../../models/quick-rating-metrics';
import { ReportService } from '../../../services/report.service';
import { Subject } from 'rxjs/internal/Subject';
import { forkJoin } from 'rxjs/internal/observable/forkJoin';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';

import { Level } from '../../../models/level';
import { EventType } from '../../../models/event-type';
import { Router } from '@angular/router';

@Component({
  selector: 'app-rating-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rating-dashboard.component.html',
  styleUrl: './rating-dashboard.component.css'
})
export class RatingDashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  loading = false;
  error: string | null = null;
  
  eventRatingsReport: EventRatingsReport | null = null;
  profileRatingsReport: ProfileRatingsReport | null = null;
  eventTypeSatisfaction: EventTypeSatisfaction[] = [];
  danceStyleSatisfaction: DanceStyleSatisfaction[] = [];
  quickMetrics: QuickRatingMetrics | null = null;

  constructor(private ratingsReportsService: ReportService, private router: Router) {}

  ngOnInit() {
    this.loadAllData();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAllData() {
    this.loading = true;
    this.error = null;

    forkJoin({
      eventRatings: this.ratingsReportsService.getEventRatingsReport(),
      profileRatings: this.ratingsReportsService.getProfileRatingsReport(),
      eventTypeSatisfaction: this.ratingsReportsService.getEventTypeSatisfactionReport(),
      danceStyleSatisfaction: this.ratingsReportsService.getDanceStyleSatisfactionReport(),
      quickMetrics: this.ratingsReportsService.getQuickRatingMetrics()
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (data) => {
        this.eventRatingsReport = data.eventRatings;
        this.profileRatingsReport = data.profileRatings;
        this.eventTypeSatisfaction = data.eventTypeSatisfaction;
        this.danceStyleSatisfaction = data.danceStyleSatisfaction;
        this.quickMetrics = data.quickMetrics;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading ratings data:', error);
        this.error = 'Error al cargar los datos de ratings. Por favor, intenta nuevamente.';
        this.loading = false;
      }
    });
  }

  refreshData() {
    this.loadAllData();
  }

  getStarsArray(starsDistribution: { [key: number]: number }) {
    const total = Object.values(starsDistribution).reduce((sum, count) => sum + count, 0);
    
    return [5, 4, 3, 2, 1].map(stars => ({
      stars,
      count: starsDistribution[stars] || 0,
      percentage: total > 0 ? ((starsDistribution[stars] || 0) / total) * 100 : 0
    }));
  }

  getEventTypeName(eventType: EventType): string {
    const names = {
      [EventType.CLASE]: 'Clase',
      [EventType.COMPETENCIA]: 'Competencia',
      [EventType.FESTIVAL]: 'Festival',
      [EventType.SOCIAL]: 'Social'
    };
    return names[eventType] || eventType;
  }

  getLevelName(level: Level): string {
    const names = {
      [Level.PRINCIPIANTE]: 'Principiante',
      [Level.INTERMEDIO]: 'Intermedio',
      [Level.AVANZADO]: 'Avanzado',
      [Level.PROFESIONAL]: 'Profesional'
    };
    return names[level] || level;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
  
}