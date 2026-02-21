import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TagResponse {
  id: number;
  name: string;
  createdAt: string;
}

export interface ShortenUrlRequest {
  longUrl: string;
  customAlias?: string;
  expiresAt?: string;
  tagIds?: number[];
}

export interface ShortenUrlResponse {
  shortUrl: string;
  longUrl: string;
  shortCode: string;
  createdAt: string;
  expiresAt?: string;
  isCustomAlias: boolean;
}

export interface UserUrlResponse {
  shortCode: string;
  shortUrl: string;
  longUrl: string;
  clickCount: number;
  createdAt: string;
  expiresAt?: string;
  isActive: boolean;
  isCustomAlias: boolean;
  tags: TagResponse[];
}

export interface AliasAvailabilityResponse {
  alias: string;
  available: boolean;
}

export interface UpdateUrlRequest {
  customAlias?: string;
  expiresAt?: string;
  clearExpiration?: boolean;
  tagIds?: number[];
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

  getUserUrls(tagId?: number): Observable<UserUrlResponse[]> {
    let params = new HttpParams();
    if (tagId !== undefined) {
      params = params.set('tagId', tagId.toString());
    }
    return this.http.get<UserUrlResponse[]>(`${this.apiUrl}/urls`, { params });
  }

  checkAliasAvailability(alias: string): Observable<AliasAvailabilityResponse> {
    return this.http.get<AliasAvailabilityResponse>(`${this.apiUrl}/urls/check-alias/${encodeURIComponent(alias)}`);
  }

  updateUrl(shortCode: string, request: UpdateUrlRequest): Observable<UserUrlResponse> {
    return this.http.patch<UserUrlResponse>(`${this.apiUrl}/urls/${shortCode}`, request);
  }

  toggleUrlStatus(shortCode: string, active: boolean): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/urls/${shortCode}/status`, { active });
  }

  deleteUrl(shortCode: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/urls/${shortCode}`);
  }
}
