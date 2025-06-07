import { EventType } from "./event-type";
import { EventStatus } from "./EventStatus";
import { ParticipantInfoDto } from "./participant-info-dto";

export interface EventDetailReportDto {
  eventId: number;
  eventName: string;
  eventType: EventType;
  eventStatus: EventStatus;
  eventDate: string;
  organizerName: string;
  cityName: string;
  countryName: string;
  capacity: number;
  totalRegistrations: number;
  confirmedRegistrations: number;
  pendingRegistrations: number;
  cancelledRegistrations: number;
  availableCapacity: number;
  occupancyRate: number;
  price: number;
  danceStyles: String[];
  totalRevenue: number;
  averageRating: number;
  totalRatings: number;
  participants: ParticipantInfoDto[];
}
