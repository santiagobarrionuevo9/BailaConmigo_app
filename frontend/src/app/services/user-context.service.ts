import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserContextService {

  get userId(): number | null {
    const id = localStorage.getItem('userId');
    return id ? Number(id) : null;
  }

  get role(): string | null {
    return localStorage.getItem('role');
  }

  get fullName(): string | null {
    return localStorage.getItem('fullName');
  }

  get token(): string | null {
    return localStorage.getItem('token');
  }

  get SubscriptionType(): string | null {
    return localStorage.getItem('subscriptionType');
  }
  get SubscriptionExpiration(): string | null {
    return localStorage.getItem('subscriptionExpiration');
  }
  get Email(): string | null {
    return localStorage.getItem('email');
  }

  clear(): void {
    localStorage.removeItem('userId');
    localStorage.removeItem('role');
    localStorage.removeItem('fullName');
    localStorage.removeItem('token');
  }
}
