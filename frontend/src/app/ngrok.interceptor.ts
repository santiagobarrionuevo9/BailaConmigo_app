import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpInterceptorFn } from '@angular/common/http';

export const ngrokInterceptor: HttpInterceptorFn = (req, next) => {
  // Agregar header para todas las requests a ngrok
  if (req.url.includes('ngrok')) {
    const ngrokReq = req.clone({
      setHeaders: {
        'ngrok-skip-browser-warning': 'true'
      }
    });
    return next(ngrokReq);
  }
  
  return next(req);
};