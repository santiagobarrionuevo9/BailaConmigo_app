import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DanceDiversityReportDto } from '../../../models/dance-diversity-report-dto';
import { ReportService } from '../../../services/report.service';

@Component({
  selector: 'app-diversity',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './diversity.component.html',
  styleUrl: './diversity.component.css'
})
export class DiversityComponent implements OnInit {
  report: DanceDiversityReportDto | null = null;
  loading = false;
  error: string | null = null;

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadReport();
  }

  loadReport(): void {
    this.loading = true;
    this.error = null;
    this.reportService.getDanceDiversityReport().subscribe({
      next: (data) => {
        this.report = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el reporte de diversidad de baile.';
        this.loading = false;
      }
    });
  }

  getStyleEmoji(styleName: string): string {
    const emojis: { [key: string]: string } = {
      Salsa: '🕺',
      Tango: '💃',
      HipHop: '🎧',
      Ballet: '🩰',
      // Agrega más estilos según sea necesario
    };
    return emojis[styleName] || '🎶';
  }

  getLevelIcon(levelName: string): string {
    const icons: { [key: string]: string } = {
      PRINCIPIANTE: '🌱',
      INTERMEDIO: '🌿',
      AVANZADO: '🌳',
      EXPERTO: '🏆',
    };
    return icons[levelName.toUpperCase()] || '🎓';
  }

  getGroupedRegionalData(): { region: string; styles: any[] }[] {
    if (!this.report?.stylesByRegion) return [];
    const grouped: { [region: string]: any[] } = {};
    this.report.stylesByRegion.forEach((item) => {
      if (!grouped[item.region]) {
        grouped[item.region] = [];
      }
      grouped[item.region].push(item);
    });
    return Object.keys(grouped).map((region) => ({
      region,
      styles: grouped[region],
    }));
  }

  getUniqueLevels(): string[] {
    if (!this.report?.styleLevelStats) return [];
    const levels = this.report.styleLevelStats.map((item) => item.levelName);
    return Array.from(new Set(levels));
  }

  getUniqueStyles(): string[] {
    if (!this.report?.styleLevelStats) return [];
    const styles = this.report.styleLevelStats.map((item) => item.styleName);
    return Array.from(new Set(styles));
  }

  getMatrixValue(styleName: string, levelName: string): number {
    if (!this.report?.styleLevelStats) return 0;
    const match = this.report.styleLevelStats.find(
      (item) => item.styleName === styleName && item.levelName === levelName
    );
    return match ? match.count : 0;
  }

  getMatrixPercentage(styleName: string, levelName: string): number {
    if (!this.report?.styleLevelStats) return 0;
    const match = this.report.styleLevelStats.find(
      (item) => item.styleName === styleName && item.levelName === levelName
    );
    return match ? match.percentage : 0;
  }
}
