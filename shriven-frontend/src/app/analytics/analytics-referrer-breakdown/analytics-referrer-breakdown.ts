import { Component, Input, OnChanges, OnInit, inject, signal } from '@angular/core';
import { AnalyticsService, ReferrerStatItem } from '../../services/analytics.service';

@Component({
  selector: 'app-analytics-referrer-breakdown',
  standalone: true,
  templateUrl: './analytics-referrer-breakdown.html',
  styleUrl: './analytics-referrer-breakdown.css'
})
export class AnalyticsReferrerBreakdownComponent implements OnInit, OnChanges {
  @Input({ required: true }) shortCode!: string;

  private readonly analyticsService = inject(AnalyticsService);

  protected readonly items = signal<ReferrerStatItem[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadStats();
  }

  ngOnChanges(): void {
    this.loadStats();
  }

  private loadStats(): void {
    if (!this.shortCode) return;
    this.loading.set(true);
    this.analyticsService.getReferrerStats(this.shortCode).subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Failed to load referrer stats');
        this.loading.set(false);
      }
    });
  }
}

