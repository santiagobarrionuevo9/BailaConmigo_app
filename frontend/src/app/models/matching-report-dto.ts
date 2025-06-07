import { MatchingPeriodStatsDto } from "./matching-period-stats-dto";
import { UserMatchingStatsDto } from "./user-matching-stats-dto";

export interface MatchingReportDto {
  totalLikesGiven: number;
  totalSuccessfulMatches: number;
  conversionRate: number;
  averageMatchesPerUser: number;
  todayStats: MatchingPeriodStatsDto;
  thisWeekStats: MatchingPeriodStatsDto;
  thisMonthStats: MatchingPeriodStatsDto;
  topActiveUsers: UserMatchingStatsDto[];
}
