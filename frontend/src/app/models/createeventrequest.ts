export interface CreateEventRequestDto {
    name: string;
    description?: string;
    dateTime: string; // formato ISO: '2025-05-01T19:00'
    
    countryId: number;
    cityId: number;
    address?: string;
    capacity?: number;
    price?: number;
    eventType?: 'CLASE' | 'COMPETENCIA' | 'FESTIVAL';
    // Campos faltantes que espera el backend:
    danceStyles?: string[]; // Conjunto de estilos de baile
    additionalInfo?: string; // Información adicional
    mediaUrls?: string[]; // URLs de medios
    
  }
  
