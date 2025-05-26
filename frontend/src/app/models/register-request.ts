import { Role } from "./role";
import { SubscriptionType } from "./subscription-type";

export interface RegisterRequestDto {
    fullName: string;
    email: string;
    password: string;
    gender: string;
    birthdate: Date;
    countryId: number;
    cityId: number;
    role: Role;
    subscriptionType?: SubscriptionType;
  }
