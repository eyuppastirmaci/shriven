import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ChartBar,
  Copy,
  Link2,
  Loader,
  LucideAngularModule,
  ShieldCheck,
  Zap
} from 'lucide-angular';
import { UrlShortenerService } from '../services/url-shortener.service';
import { ToastService } from '../shared/toast/toast.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    FormsModule,
    LucideAngularModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  protected readonly linkIcon = Link2;
  protected readonly copyIcon = Copy;
  protected readonly loaderIcon = Loader;
  protected readonly zapIcon = Zap;
  protected readonly chartBarIcon = ChartBar;
  protected readonly shieldCheckIcon = ShieldCheck;
  private readonly urlShortenerService = inject(UrlShortenerService);
  private readonly toastService = inject(ToastService);

  protected readonly longUrl = signal('');
  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly shortUrl = signal<string | null>(null);

  get isValidUrl(): boolean {
    const url = this.longUrl();
    if (!url) return false;
    try {
      const urlObj = new URL(url);
      return urlObj.protocol === 'http:' || urlObj.protocol === 'https:';
    } catch {
      return false;
    }
  }

  onShortenUrl(): void {
    if (!this.isValidUrl) {
      this.error.set('Please enter a valid URL starting with http:// or https://');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);
    this.shortUrl.set(null);

    this.urlShortenerService.shortenUrl({ longUrl: this.longUrl() }).subscribe({
      next: (response) => {
        this.shortUrl.set(response.shortUrl);
        this.isLoading.set(false);
        this.longUrl.set('');
        this.toastService.show('Short link created!', 'success');
      },
      error: (err) => {
        console.error('Error shortening URL:', err);
        this.error.set(err.error?.message || 'Failed to shorten URL. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  onCopyToClipboard(): void {
    const url = this.shortUrl();
    if (url) {
      navigator.clipboard.writeText(url);
      this.toastService.show('Copied to clipboard!', 'success');
    }
  }
}
