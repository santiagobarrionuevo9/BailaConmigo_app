import { DanceStyleDistributionDto } from "./dance-style-distribution-dto";
import { LevelDistributionDto } from "./level-distribution-dto";
import { RegionalDanceStyleDto } from "./regional-dance-style-dto";
import { StyleCombinationDto } from "./style-combination-dto";
import { StyleLevelStatsDto } from "./style-level-stats-dto";

export interface DanceDiversityReportDto {
  totalActiveDancers: number;
  averageStylesPerDancer: number;
  styleDistribution: DanceStyleDistributionDto[];
  levelDistribution: LevelDistributionDto[];
  topCombinations: StyleCombinationDto[];
  stylesByRegion: RegionalDanceStyleDto[];
  styleLevelStats: StyleLevelStatsDto[];
}
