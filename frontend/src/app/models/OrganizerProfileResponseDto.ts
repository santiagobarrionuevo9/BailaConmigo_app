export interface OrganizerProfileResponseDto {
  id: number;
  organizationName: string;
  contactEmail: string;
  contactPhone: string;
  description: string;
  website: string;
  mediaUrls: string[];
  cityName: string;
  countryName: string;
  fullName: string;
}