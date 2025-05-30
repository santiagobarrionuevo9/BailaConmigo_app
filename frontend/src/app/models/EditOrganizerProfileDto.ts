export interface EditOrganizerProfileDto {
  organizationName: string;
  contactEmail: string;
  contactPhone: string;
  description: string;
  website: string;
  mediaUrls: string[];
  cityId: number;
  countryId: number;
}