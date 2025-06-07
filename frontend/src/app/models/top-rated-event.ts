import { EventType } from "@angular/router";

export interface TopRatedEvent {
  eventId: number;
  eventName: string;
  eventType: 'CLASE' | 'COMPETENCIA' | 'FESTIVAL' | 'SOCIAL';
  averageRating: number;
  totalRatings: number;
}
