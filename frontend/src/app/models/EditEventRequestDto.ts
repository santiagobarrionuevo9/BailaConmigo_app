export interface EditEventRequestDto {
  name?: string;
  description?: string;
  dateTime?: string;
  countryId: number;
  cityId: number;
  address?: string;
  capacity?: number;
  price?: number;
  danceStyles?: string[];
  additionalInfo?: string;
}
