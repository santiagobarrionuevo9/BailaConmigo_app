export interface EventResponseDto {
    id: number;
    name: string;
    description?: string;
    dateTime: string;
    location?: string;
    address?: string;
    capacity?: number;
    price?: number;
    eventType: string;
    organizerId: number;
  }
  