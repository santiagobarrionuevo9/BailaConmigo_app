import { EventStatus } from "./EventStatus";

export interface EventResponseDto {
  id: number;
  name: string;
  description?: string;
  dateTime: string;
  countryId: number;
  cityId: number;
  cityName?: string; // Asumiendo que el backend devuelve el nombre de la ciudad
  countryName?: string; // Asumiendo que el backend devuelve el nombre del país
  address?: string;
  capacity?: number;
  price?: number;
  eventType: string;
  organizerId: number;
  danceStyles?: { id: number; name: string }[]; // Asumiendo que DanceStyle tiene id y name
  additionalInfo?: string;
  mediaUrls?: string[];
  organizerName?: string;
  averageRating?: number;
  status: EventStatus;
  availableCapacity?: number;
}

