import { Component, OnInit, Output, EventEmitter, inject, signal } from '@angular/core';
import { UrlShortenerService, UserUrlResponse } from '../../services/url-shortener.service';

@Component({
  selector: 'app-analytics-link-selector',
  standalone: true,
  templateUrl: './analytics-link-selector.html',
  styleUrl: './analytics-link-selector.css'
})
export class AnalyticsLinkSelectorComponent implements OnInit {
  @Output() shortCodeChange = new EventEmitter<string>();

  private readonly urlService = inject(UrlShortenerService);

  protected readonly links = signal<UserUrlResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly selectedCode = signal<string>('');

  ngOnInit(): void {
    this.urlService.getUserUrls().subscribe({
      next: (list) => {
        this.links.set(list);
        this.loading.set(false);
        if (list.length > 0 && !this.selectedCode()) {
          this.select(list[0].shortCode);
        }
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Failed to load links');
        this.loading.set(false);
      }
    });
  }

  select(shortCode: string): void {
    this.selectedCode.set(shortCode);
    this.shortCodeChange.emit(shortCode);
  }
}
