import { TopRatedEvent } from "./top-rated-event";

export interface EventRatingsReport {
  generatedAt: string;
  totalEventRatings: number;
  averageEventRating: number;
  starsDistribution: { [key: number]: number };
  commentPercentage: number;
  topRatedEvents: TopRatedEvent[];
  mostRatedEvents: TopRatedEvent[];
}