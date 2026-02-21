import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { KeyRound, Link2, LucideAngularModule, Unlock as UnlockIcon } from 'lucide-angular';
import { UrlShortenerService } from '../services/url-shortener.service';

@Component({
  selector: 'app-unlock',
  standalone: true,
  imports: [FormsModule, RouterLink, LucideAngularModule],
  templateUrl: './unlock.html',
  styleUrl: './unlock.css'
})
export class Unlock implements OnInit {
  protected readonly keyIcon = KeyRound;
  protected readonly unlockIcon = UnlockIcon;
  protected readonly linkIcon = Link2;

  private readonly route = inject(ActivatedRoute);
  private readonly urlService = inject(UrlShortenerService);

  protected readonly code = signal<string | null>(null);
  protected readonly password = signal('');
  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');
    this.code.set(code);
    if (!code) {
      this.error.set('No link code provided. Use the link you were given.');
    } else {
      this.error.set(null);
    }
  }

  protected onSubmit(): void {
    const code = this.code();
    const pwd = this.password()?.trim();
    if (!code || !pwd) {
      this.error.set('Please enter the password.');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    this.urlService.unlockLink(code, pwd).subscribe({
      next: (response) => {
        window.location.href = response.redirectUrl;
      },
      error: (err) => {
        this.isLoading.set(false);
        const errorCode = err.error?.errorCode;
        const message = err.error?.message;
        if (errorCode === 'INVALID_LINK_PASSWORD' || err.status === 401) {
          this.error.set(message || 'Wrong password. Please try again.');
        } else if (err.status === 404 || errorCode === 'URL_NOT_FOUND') {
          this.error.set(message || 'Link not found or no longer available.');
        } else if (err.status === 410 || errorCode === 'URL_EXPIRED' || errorCode === 'URL_PAUSED') {
          this.error.set(message || 'This link has expired or is paused.');
        } else {
          this.error.set(message || 'Something went wrong. Please try again.');
        }
      }
    });
  }
}
