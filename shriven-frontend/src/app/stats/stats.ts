import { Component, OnInit, PLATFORM_ID, signal, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { ArrowLeft, BarChart2, LucideAngularModule, Loader } from 'lucide-angular';
import { AnalyticsService, StatsResponse } from '../services/analytics.service';

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [BaseChartDirective, RouterLink, LucideAngularModule],
  providers: [provideCharts(withDefaultRegisterables())],
  templateUrl: './stats.html',
  styleUrl: './stats.css',
})
export class Stats implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly analyticsService = inject(AnalyticsService);
  protected readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  protected readonly arrowLeftIcon = ArrowLeft;
  protected readonly chartIcon = BarChart2;
  protected readonly loaderIcon = Loader;

  protected readonly shortCode = signal('');
  protected readonly period = signal<'daily' | 'weekly'>('daily');
  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly totalClicks = signal(0);

  protected chartData = signal<ChartData<'bar'>>({
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Clicks',
        backgroundColor: 'rgba(74, 222, 128, 0.5)',
        borderColor: '#4ade80',
        borderWidth: 1,
        borderRadius: 4,
      },
    ],
  });

  protected readonly chartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1a1a1a',
        borderColor: '#3a3a3a',
        borderWidth: 1,
        titleColor: '#f5f5f5',
        bodyColor: '#4ade80',
        padding: { x: 14, y: 10 },
        cornerRadius: 8,
        displayColors: false,
        callbacks: {
          title: (items) => {
            if (!items.length) return '';
            const label = items[0].label;
            if (label.startsWith('Week of ')) {
              const datePart = label.replace('Week of ', '');
              return `Week of ${this.expandDate(datePart)}`;
            }
            return this.expandDate(label);
          },
          label: (item) => `${item.raw} clicks`,
        },
      },
    },
    scales: {
      x: {
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#a0a0a0' },
        border: { color: '#2a2a2a' },
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#a0a0a0', precision: 0 },
        border: { color: '#2a2a2a' },
      },
    },
  };

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('shortCode') ?? '';
    this.shortCode.set(code);
    this.loadStats();
  }

  protected setPeriod(period: 'daily' | 'weekly'): void {
    if (this.period() === period) return;
    this.period.set(period);
    this.loadStats();
  }

  private loadStats(): void {
    const code = this.shortCode();
    if (!code) return;

    this.isLoading.set(true);
    this.error.set(null);

    this.analyticsService.getStats(code, this.period()).subscribe({
      next: (response) => {
        this.totalClicks.set(response.totalClicks);
        this.updateChart(response);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Failed to load stats. Please try again.');
        this.isLoading.set(false);
      },
    });
  }

  private updateChart(response: StatsResponse): void {
    if (this.period() === 'daily') {
      const labels = this.generateLast7DayLabels();
      const clickMap = new Map(response.dailyStats.map((s) => [s.date, s.clicks]));
      const data = labels.map((label) => clickMap.get(label) ?? 0);
      this.chartData.set({
        labels: labels.map((d) => this.formatDate(d)),
        datasets: [{ ...this.chartData().datasets[0], data, label: 'Daily Clicks' }],
      });
    } else {
      const sorted = [...response.weeklyStats].sort((a, b) =>
        a.weekStart.localeCompare(b.weekStart),
      );
      this.chartData.set({
        labels: sorted.map((s) => `Week of ${this.formatDate(s.weekStart)}`),
        datasets: [
          { ...this.chartData().datasets[0], data: sorted.map((s) => s.clicks), label: 'Weekly Clicks' },
        ],
      });
    }
  }

  private generateLast7DayLabels(): string[] {
    const labels: string[] = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      labels.push(d.toISOString().split('T')[0]);
    }
    return labels;
  }

  private formatDate(isoDate: string): string {
    const [, month, day] = isoDate.split('-');
    return `${month}/${day}`;
  }

  private expandDate(mmdd: string): string {
    const [month, day] = mmdd.split('/').map(Number);
    const year = new Date().getFullYear();
    return new Date(year, month - 1, day).toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    });
  }
}
