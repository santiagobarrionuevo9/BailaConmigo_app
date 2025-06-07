export interface UpcomingEventReportDto {
  eventId: number;
  eventName: string;
  eventDate: string;
  organizerName: string;
  cityName: string;
  capacity: number;
  currentRegistrations: number;
  availableSpots: number;
  occupancyRate: number;
}
