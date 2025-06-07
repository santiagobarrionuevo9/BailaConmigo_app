import { DanceStyleDistributionDto } from "./dance-style-distribution-dto";

export interface EnhancedGeographicDto {
  name: string;
  userCount: number;
  dancerCount: number;
  percentage: number;
  popularStyles: DanceStyleDistributionDto[];
}
