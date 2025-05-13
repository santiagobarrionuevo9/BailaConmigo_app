export interface EditEventRequestDto {
  name?: string;
  description?: string;
  dateTime?: string;
  location?: string;
  address?: string;
  capacity?: number;
  price?: number;
  danceStyles?: string[];
  additionalInfo?: string;
}
