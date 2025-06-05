export interface EditDancerProfileDto {
    
    cityId: number;
    countryId: number;
    danceStyles: string[]; // enum
    level: string; // enum
    aboutMe: string;
    availability: string;
    mediaUrls: string[]; // URLs de fotos/videos
  }
  