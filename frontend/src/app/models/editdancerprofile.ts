export interface EditDancerProfileDto {
    city: string;
    danceStyles: string[]; // enum
    level: string; // enum
    aboutMe: string;
    availability: string;
    mediaUrls: string[]; // URLs de fotos/videos
  }
  