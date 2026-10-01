import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  get<T>(path: string, params?: Record<string, string | number | boolean>): Observable<T> {
    let hp = new HttpParams();
    if (params) Object.entries(params).forEach(([k, v]) => hp = hp.set(k, String(v)));
    return this.http.get<T>(`${this.base}${path}`, { params: hp });
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<T>(`${this.base}${path}`, body);
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<T>(`${this.base}${path}`, body);
  }

  patch<T>(path: string, body?: unknown, params?: Record<string, string>): Observable<T> {
    let hp = new HttpParams();
    if (params) Object.entries(params).forEach(([k, v]) => hp = hp.set(k, v));
    return this.http.patch<T>(`${this.base}${path}`, body ?? {}, { params: hp });
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<T>(`${this.base}${path}`);
  }

  getBlob(path: string): Observable<Blob> {
    return this.http.get(`${this.base}${path}`, { responseType: 'blob' });
  }
}
