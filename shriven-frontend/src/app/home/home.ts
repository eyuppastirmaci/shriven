import { Component, OnDestroy, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  ChartBar,
  Check,
  ChevronDown,
  ChevronUp,
  Copy,
  Link2,
  Loader,
  LucideAngularModule,
  ShieldCheck,
  Zap
} from 'lucide-angular';
import { AuthService } from '../services/auth.service';
import { ShortenUrlResponse, UrlShortenerService, UserUrlResponse } from '../services/url-shortener.service';
import { ToastService } from '../shared/toast/toast.service';
import { ShortenerAdvancedOptionsComponent } from '../shared/shortener-advanced-options/shortener-advanced-options';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    FormsModule,
    LucideAngularModule,
    RouterLink,
    ShortenerAdvancedOptionsComponent
  ],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnDestroy {
  protected readonly linkIcon = Link2;
  protected readonly copyIcon = Copy;
  protected readonly loaderIcon = Loader;
  protected readonly zapIcon = Zap;
  protected readonly chartBarIcon = ChartBar;
  protected readonly shieldCheckIcon = ShieldCheck;
  protected readonly checkIcon = Check;
  protected readonly chevronDownIcon = ChevronDown;
  protected readonly chevronUpIcon = ChevronUp;

  protected readonly authService = inject(AuthService);
  private readonly urlShortenerService = inject(UrlShortenerService);
  private readonly toastService = inject(ToastService);

  protected readonly longUrl = signal('');
  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly shortUrl = signal<string | null>(null);
  protected readonly shortCode = signal<string | null>(null);

  protected readonly showAdvanced = signal(false);
  protected readonly customAlias = signal('');
  protected readonly expiresAt = signal('');
  protected readonly aliasAvailable = signal<boolean | null>(null);

  protected readonly duplicateUrl = signal<UserUrlResponse | null>(null);
  protected readonly copied = signal(false);

  private copyTimeout: ReturnType<typeof setTimeout> | null = null;

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

  ngOnDestroy(): void {
    if (this.copyTimeout) clearTimeout(this.copyTimeout);
  }

  protected isAliasFormatValid(alias: string): boolean {
    return /^[a-zA-Z0-9_-]{3,30}$/.test(alias);
  }

  onShortenUrl(): void {
    if (!this.isValidUrl) {
      this.error.set('Please enter a valid URL starting with http:// or https://');
      return;
    }

    const alias = this.customAlias();
    if (alias && !this.isAliasFormatValid(alias)) {
      this.error.set('Please fix the custom alias before shortening.');
      return;
    }
    if (alias && this.aliasAvailable() === false) {
      this.error.set('This alias is already taken. Please choose a different one.');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);
    this.shortUrl.set(null);
    this.duplicateUrl.set(null);

    const expiresAtValue = this.expiresAt();
    const request = {
      longUrl: this.longUrl(),
      ...(alias ? { customAlias: alias } : {}),
      ...(expiresAtValue ? { expiresAt: new Date(expiresAtValue).toISOString() } : {})
    };

    this.urlShortenerService.shortenUrl(request).subscribe({
      next: (response: ShortenUrlResponse) => {
        this.shortUrl.set(response.shortUrl);
        this.shortCode.set(response.shortCode);
        this.isLoading.set(false);
        this.longUrl.set('');
        this.customAlias.set('');
        this.expiresAt.set('');
        this.aliasAvailable.set(null);
        this.showAdvanced.set(false);
        this.toastService.show('Short link created!', 'success');
      },
      error: (err) => {
        this.isLoading.set(false);
        const errorCode = err.error?.errorCode;
        if (errorCode === 'DUPLICATE_LINK' && err.error?.data) {
          this.duplicateUrl.set(err.error.data as UserUrlResponse);
          this.error.set(null);
        } else {
          this.error.set(err.error?.message || 'Failed to shorten URL. Please try again.');
        }
      }
    });
  }

  onUseDuplicate(): void {
    const dup = this.duplicateUrl();
    if (dup) {
      this.shortUrl.set(dup.shortUrl);
      this.shortCode.set(dup.shortCode);
      this.duplicateUrl.set(null);
      this.longUrl.set('');
    }
  }

  onCopyToClipboard(): void {
    const url = this.shortUrl();
    if (url) {
      navigator.clipboard.writeText(url);
      this.toastService.show('Copied to clipboard!', 'success');
      this.copied.set(true);
      if (this.copyTimeout) clearTimeout(this.copyTimeout);
      this.copyTimeout = setTimeout(() => this.copied.set(false), 2000);
    }
  }
}
