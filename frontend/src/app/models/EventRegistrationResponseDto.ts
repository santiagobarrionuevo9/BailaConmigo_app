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
}