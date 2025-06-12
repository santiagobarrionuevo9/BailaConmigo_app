import { RegistrationStatus } from "./RegistrationStatus";

export interface EventRegistrationResponseDto {
  id: number;
  eventId: number;
  eventName: string;
  eventDateTime: Date;
  dancerId: number;
  dancerName: string;
  registrationDate: Date;
  status: RegistrationStatus;
  codigoDinamico: string;
  paidAmount: number;
  appFee: number;
  organizerAmount: number;
  paymentPreferenceId: string;
  paymentId: string;
  paymentStatus: string;
  paymentReference: string;
  paymentDate: string;
  paymentMethod: string;
  attended: boolean;
  paymentUrl: string;
}