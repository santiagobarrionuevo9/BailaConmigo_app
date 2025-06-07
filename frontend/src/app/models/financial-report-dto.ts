import { EventType } from "./event-type";

export interface FinancialReportDto {
  startDate: string;
  endDate: string;
  generatedAt: string;
  totalRevenue: number;
  totalAppFees: number;
  totalOrganizerAmount: number;
  transactionsByPaymentMethod: Record<string, number>;
  revenueByEventType: Record<EventType, number>;
}
