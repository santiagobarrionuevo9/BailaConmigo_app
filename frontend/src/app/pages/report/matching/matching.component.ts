import { Component, OnInit } from '@angular/core';
import { ReportService } from '../../../services/report.service';
import { MatchingReportDto } from '../../../models/matching-report-dto';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-matching',
  standalone: true,
  imports: [CommonModule, FormsModule ],
  templateUrl: './matching.component.html',
  styleUrl: './matching.component.css'
})
export class MatchingComponent implements OnInit {
  report: MatchingReportDto | null = null;
  loading = false;
  error: string | null = null;
  
  startDate: string = '';
  endDate: string = '';

  constructor(private reportsService: ReportService) {}

  ngOnInit(): void {
    this.loadReport();
  }

  loadReport(): void {
    this.loading = true;
    this.error = null;

    const reportObservable = this.startDate && this.endDate 
      ? this.reportsService.getMatchingReportByDateRange(this.startDate, this.endDate)
      : this.reportsService.getMatchingActivityReport();

    reportObservable.subscribe({
      next: (data) => {
        this.report = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'No se pudo cargar el reporte de matching';
        this.loading = false;
        console.error('Error loading matching report:', err);
      }
    });
  }

  onDateChange(): void {
    if (this.startDate && this.endDate) {
      if (new Date(this.startDate) > new Date(this.endDate)) {
        this.error = 'La fecha de inicio no puede ser posterior a la fecha de fin';
        return;
      }
      this.loadReport();
    }
  }

  resetFilters(): void {
    this.startDate = '';
    this.endDate = '';
    this.loadReport();
  }

  getInitials(fullName: string): string {
    return fullName
      .split(' ')
      .map(name => name.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }
}
