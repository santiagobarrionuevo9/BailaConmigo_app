import { CityStyleRankingDto } from "./city-style-ranking-dto";
import { EnhancedGeographicDto } from "./enhanced-geographic-dto";

export interface GeographicReportDto {
  totalUsers: number;
  totalDancers: number;
  totalCities: number;
  totalCountries: number;
  topCities: EnhancedGeographicDto[];
  topCountries: EnhancedGeographicDto[];
  cityStyleRankings: CityStyleRankingDto[];
}