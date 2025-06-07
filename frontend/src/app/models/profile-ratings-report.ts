import { TopRatedProfile } from "./top-rated-profile";

export interface ProfileRatingsReport {
  generatedAt: string;
  totalProfileRatings: number;
  averageProfileRating: number;
  starsDistribution: { [key: number]: number };
  commentPercentage: number;
  topRatedProfiles: TopRatedProfile[];
  mostRatedProfiles: TopRatedProfile[];
}