export interface City {
  id: number;
  name: string;
  countryId: number;
  countryName: string;
  countryCode: string;
  latitude?: number;
  longitude?: number;
}