import { EventType } from "@angular/router";

export interface EventTypeSatisfaction {
  eventType: EventType;
  eventTypeName: string;
  averageRating: number;
  totalRatedEvents: number;
  totalEvents: number;
}