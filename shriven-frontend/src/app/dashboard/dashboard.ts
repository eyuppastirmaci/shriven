import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  BarChart2,
  Check,
  Copy,
  ExternalLink,
  LayoutDashboard,
  Link2,
  LogOut,
  LucideAngularModule,
  Trash2
} from 'lucide-angular';
import { AuthService } from '../services/auth.service';
import { UrlShortenerService, UserUrlResponse } from '../services/url-shortener.service';
import { ToastService } from '../shared/toast/toast.service';
import { TooltipDirective } from '../shared/tooltip/tooltip.directive';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, TooltipDirective],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  protected readonly dashboardIcon = LayoutDashboard;
  protected readonly linkIcon = Link2;
  protected readonly copyIcon = Copy;
  protected readonly statsIcon = BarChart2;
  protected readonly trashIcon = Trash2;
  protected readonly externalLinkIcon = ExternalLink;
  protected readonly logoutIcon = LogOut;
  protected readonly checkIcon = Check;

  protected readonly authService = inject(AuthService);
  private readonly urlService = inject(UrlShortenerService);
  private readonly toastService = inject(ToastService);

  protected readonly urls = signal<UserUrlResponse[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly deletingCode = signal<string | null>(null);
  protected readonly copiedCode = signal<string | null>(null);
  private copyTimeout: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.loadUrls();
  }

  protected loadUrls(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.urlService.getUserUrls().subscribe({
      next: (data) => {
        this.urls.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Failed to load your links. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  onCopy(shortUrl: string, shortCode: string): void {
    navigator.clipboard.writeText(shortUrl);
    this.toastService.show('Copied to clipboard!', 'success');

    if (this.copyTimeout) clearTimeout(this.copyTimeout);
    this.copiedCode.set(shortCode);
    this.copyTimeout = setTimeout(() => this.copiedCode.set(null), 2000);
  }

  onDelete(shortCode: string): void {
    this.deletingCode.set(shortCode);

    this.urlService.deleteUrl(shortCode).subscribe({
      next: () => {
        this.urls.update(list => list.filter(u => u.shortCode !== shortCode));
        this.deletingCode.set(null);
        this.toastService.show('Link deleted', 'success');
      },
      error: () => {
        this.deletingCode.set(null);
        this.toastService.show('Failed to delete link', 'error');
      }
    });
  }

  onLogout(): void {
    this.authService.logout();
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  }
}
