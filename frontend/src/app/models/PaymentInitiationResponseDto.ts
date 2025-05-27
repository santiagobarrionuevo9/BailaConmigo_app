export interface PaymentInitiationResponseDto {
  registrationId: number;
  preferenceId: string;
  paymentUrl: string;
  totalAmount: number;
  appFee: number;
  organizerAmount: number;
  eventName: string;
  message: string;
}