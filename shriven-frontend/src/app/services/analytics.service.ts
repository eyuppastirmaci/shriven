import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DailyStat {
  date: string;
  clicks: number;
}

export interface WeeklyStat {
  weekStart: string;
  clicks: number;
}

export interface StatsResponse {
  shortCode: string;
  totalClicks: number;
  dailyStats: DailyStat[];
  weeklyStats: WeeklyStat[];
}

export interface GeoStatItem {
  countryCode: string;
  countryName: string | null;
  count: number;
}

export interface DeviceStatItem {
  browser: string;
  os: string;
  deviceType: string;
  count: number;
}

export interface ReferrerStatItem {
  referrerDomain: string;
  count: number;
}

@Injectable({
  providedIn: 'root',
})
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getStats(shortCode: string, period: string): Observable<StatsResponse> {
    const params = new HttpParams().set('period', period);
    return this.http.get<StatsResponse>(`${this.apiUrl}/stats/${shortCode}`, { params });
  }

  getGeoStats(shortCode: string): Observable<GeoStatItem[]> {
    return this.http.get<GeoStatItem[]>(`${this.apiUrl}/stats/${shortCode}/geo`);
  }

  getDeviceStats(shortCode: string): Observable<DeviceStatItem[]> {
    return this.http.get<DeviceStatItem[]>(`${this.apiUrl}/stats/${shortCode}/devices`);
  }

  getReferrerStats(shortCode: string): Observable<ReferrerStatItem[]> {
    return this.http.get<ReferrerStatItem[]>(`${this.apiUrl}/stats/${shortCode}/referrers`);
  }
}
