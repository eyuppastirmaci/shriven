import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ShortenUrlRequest {
  longUrl: string;
  expiresAt?: string;
}

export interface ShortenUrlResponse {
  shortUrl: string;
  longUrl: string;
  shortCode: string;
  createdAt: string;
  expiresAt?: string;
}

export interface UserUrlResponse {
  shortCode: string;
  shortUrl: string;
  longUrl: string;
  clickCount: number;
  createdAt: string;
  expiresAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UrlShortenerService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  shortenUrl(request: ShortenUrlRequest): Observable<ShortenUrlResponse> {
    return this.http.post<ShortenUrlResponse>(`${this.apiUrl}/shorten`, request);
  }

  getUserUrls(): Observable<UserUrlResponse[]> {
    return this.http.get<UserUrlResponse[]>(`${this.apiUrl}/urls`);
  }

  deleteUrl(shortCode: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/urls/${shortCode}`);
  }
}
