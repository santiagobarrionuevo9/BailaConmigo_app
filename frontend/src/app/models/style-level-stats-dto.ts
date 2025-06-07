import { Level } from "./level";

export interface StyleLevelStatsDto {
  level: Level;
  levelName: string;
  style: String;
  styleName: string;
  count: number;
  percentage: number;
}
