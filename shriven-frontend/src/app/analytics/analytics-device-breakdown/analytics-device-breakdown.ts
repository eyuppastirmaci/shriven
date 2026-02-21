import { Component, Input, OnChanges, OnInit, inject, signal } from '@angular/core';
import { AnalyticsService, DeviceStatItem } from '../../services/analytics.service';

@Component({
  selector: 'app-analytics-device-breakdown',
  standalone: true,
  templateUrl: './analytics-device-breakdown.html',
  styleUrl: './analytics-device-breakdown.css'
})
export class AnalyticsDeviceBreakdownComponent implements OnInit, OnChanges {
  @Input({ required: true }) shortCode!: string;

  private readonly analyticsService = inject(AnalyticsService);

  protected readonly items = signal<DeviceStatItem[]>([]);
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
    this.analyticsService.getDeviceStats(this.shortCode).subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Failed to load device stats');
        this.loading.set(false);
      }
    });
  }
}

