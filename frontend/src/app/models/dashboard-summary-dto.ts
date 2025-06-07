import { UpcomingEventReportDto } from "./upcoming-event-report-dto";

export interface DashboardSummaryDto {
  totalEvents: number;
  totalRegistrations: number;
  totalRevenue: number;
  upcomingEvents: UpcomingEventReportDto[];
}
