import { DanceStyleSatisfaction } from "./dance-style-satisfaction";
import { EventRatingsReport } from "./event-ratings-report";
import { EventTypeSatisfaction } from "./event-type-satisfaction";
import { ProfileRatingsReport } from "./profile-ratings-report";
import { QuickRatingMetrics } from "./quick-rating-metrics";

export interface RatingDashboard {
  eventRatings: EventRatingsReport;
  profileRatings: ProfileRatingsReport;
  eventTypeSatisfaction: EventTypeSatisfaction[];
  danceStyleSatisfaction: DanceStyleSatisfaction[];
  quickMetrics: QuickRatingMetrics;
}
