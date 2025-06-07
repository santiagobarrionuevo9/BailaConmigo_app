import { EventType } from "./event-type";

export interface EventGeneralReportDto {
  generatedAt: string;
  totalEvents: number;
  activeEvents: number;
  cancelledEvents: number;
  totalRegistrations: number;
  confirmedRegistrations: number;
  pendingRegistrations: number;
  cancelledRegistrations: number;
  totalRevenue: number;
  averageRegistrationsPerEvent: number;
  eventsByType: Record<EventType, number>;
  danceStylePopularity: Record<string, number>;
}
