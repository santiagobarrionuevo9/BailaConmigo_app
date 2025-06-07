import { Level } from "./level";

export interface LevelDistributionDto {
  level: Level;
  levelName: string;
  count: number;
  percentage: number;
}