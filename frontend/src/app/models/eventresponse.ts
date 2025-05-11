export interface EventResponseDto {
  id: number;
  name: string;
  description?: string;
  dateTime: string;
  location: string;
  address?: string;
  capacity?: number;
  price?: number;
  eventType: string;
  organizerId: number;
  danceStyles?: { id: number; name: string }[]; // Asumiendo que DanceStyle tiene id y name
  additionalInfo?: string;
  mediaUrls?: string[];
  organizerName?: string;
  averageRating?: number;
}
