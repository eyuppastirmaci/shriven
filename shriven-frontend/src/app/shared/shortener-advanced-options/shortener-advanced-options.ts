import { Component, input, output, signal, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AtSign, Calendar, Check, LucideAngularModule, X } from 'lucide-angular';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { UrlShortenerService } from '../../services/url-shortener.service';

@Component({
  selector: 'app-shortener-advanced-options',
  standalone: true,
  imports: [FormsModule, LucideAngularModule],
  templateUrl: './shortener-advanced-options.html',
  styleUrl: './shortener-advanced-options.css'
})
export class ShortenerAdvancedOptionsComponent implements OnDestroy {
  readonly customAlias = input<string>('');
  readonly expiresAt = input<string>('');
  readonly aliasChecking = input<boolean>(false);
  readonly aliasAvailable = input<boolean | null>(null);

  readonly customAliasChange = output<string>();
  readonly expiresAtChange = output<string>();
  readonly aliasAvailableChange = output<boolean | null>();

  protected readonly atSignIcon = AtSign;
  protected readonly calendarIcon = Calendar;
  protected readonly checkIcon = Check;
  protected readonly xIcon = X;

  protected readonly checking = signal(false);
  protected readonly available = signal<boolean | null>(null);

  private readonly urlService = inject(UrlShortenerService);
  private readonly aliasSubject = new Subject<string>();
  private sub?: Subscription;

  constructor() {
    this.sub = this.aliasSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(alias => {
        if (!alias || !this.isAliasFormatValid(alias)) {
          this.available.set(null);
          this.checking.set(false);
          return [];
        }
        this.checking.set(true);
        return this.urlService.checkAliasAvailability(alias);
      })
    ).subscribe({
      next: (result) => {
        this.checking.set(false);
        this.available.set(result.available);
        this.aliasAvailableChange.emit(result.available);
      },
      error: () => {
        this.checking.set(false);
        this.available.set(null);
        this.aliasAvailableChange.emit(null);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  get minDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  get aliasValidationMessage(): string | null {
    const a = this.customAlias();
    if (!a) return null;
    if (!this.isAliasFormatValid(a)) {
      return 'Must be 3–30 characters: letters, numbers, hyphens, underscores only';
    }
    return null;
  }

  protected isAliasFormatValid(alias: string): boolean {
    return /^[a-zA-Z0-9_-]{3,30}$/.test(alias);
  }

  protected onAliasChange(value: string): void {
    this.available.set(null);
    this.aliasAvailableChange.emit(null);
    this.customAliasChange.emit(value);
    if (value) this.aliasSubject.next(value);
  }

  protected onExpiryChange(value: string): void {
    this.expiresAtChange.emit(value);
  }
}
