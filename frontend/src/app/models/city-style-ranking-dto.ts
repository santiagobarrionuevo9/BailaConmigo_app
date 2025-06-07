import { GeographicDto } from "./geographic-dto";

export interface CityStyleRankingDto {
  style: String;
  styleName: string;
  topCities: GeographicDto[];
}
