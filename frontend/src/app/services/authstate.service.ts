import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/internal/BehaviorSubject';

@Injectable({
  providedIn: 'root'
})
export class AuthstateService {

  private isLoggedInSubject = new BehaviorSubject<boolean>(!!localStorage.getItem('token'));
  private roleSubject = new BehaviorSubject<string>(localStorage.getItem('role') || '');

  isLoggedIn$ = this.isLoggedInSubject.asObservable();
  userRole$ = this.roleSubject.asObservable();

  updateAuthState(isLoggedIn: boolean, role: string = '') {
    this.isLoggedInSubject.next(isLoggedIn);
    this.roleSubject.next(role);
  }
}
