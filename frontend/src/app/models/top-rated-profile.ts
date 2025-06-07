import { Level } from "./level";

export interface TopRatedProfile {
  profileId: number;
  profileName: string;
  level: Level;
  averageRating: number;
  totalRatings: number;
  danceStyles: String[];
}