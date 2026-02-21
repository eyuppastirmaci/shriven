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
}
