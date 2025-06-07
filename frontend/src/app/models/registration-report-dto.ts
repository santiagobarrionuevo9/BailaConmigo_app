export interface RegistrationReportDto {
  startDate: string;
  endDate: string;
  generatedAt: string;
  totalRegistrations: number;
  confirmedRegistrations: number;
  pendingRegistrations: number;
  cancelledRegistrations: number;
  periodRevenue: number;
  registrationsByDay: Record<string, number>;
}
