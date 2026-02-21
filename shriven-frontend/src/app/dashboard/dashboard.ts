import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
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
  Pause,
  Pencil,
  Play,
  Trash2
} from 'lucide-angular';
import { AuthService } from '../services/auth.service';
import { TagService } from '../services/tag.service';
import { UrlShortenerService, UserUrlResponse } from '../services/url-shortener.service';
import { ToastService } from '../shared/toast/toast.service';
import { TooltipDirective } from '../shared/tooltip/tooltip.directive';
import { TagFilterBarComponent } from '../shared/tag-filter-bar/tag-filter-bar';
import { TagChipsComponent } from '../shared/tag-chips/tag-chips';
import { EditLinkModalComponent } from './edit-link-modal/edit-link-modal';
import type { TagResponse } from '../services/url-shortener.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    LucideAngularModule,
    TooltipDirective,
    TagFilterBarComponent,
    TagChipsComponent,
    EditLinkModalComponent
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, OnDestroy {
  protected readonly dashboardIcon = LayoutDashboard;
  protected readonly linkIcon = Link2;
  protected readonly copyIcon = Copy;
  protected readonly statsIcon = BarChart2;
  protected readonly trashIcon = Trash2;
  protected readonly externalLinkIcon = ExternalLink;
  protected readonly logoutIcon = LogOut;
  protected readonly checkIcon = Check;
  protected readonly pauseIcon = Pause;
  protected readonly playIcon = Play;
  protected readonly pencilIcon = Pencil;

  protected readonly authService = inject(AuthService);
  private readonly urlService = inject(UrlShortenerService);
  private readonly tagService = inject(TagService);
  private readonly toastService = inject(ToastService);

  protected readonly urls = signal<UserUrlResponse[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly deletingCode = signal<string | null>(null);
  protected readonly copiedCode = signal<string | null>(null);
  protected readonly togglingCode = signal<string | null>(null);

  protected readonly userTags = signal<TagResponse[]>([]);
  protected readonly selectedTagFilter = signal<number | null>(null);
  protected readonly editingUrl = signal<UserUrlResponse | null>(null);

  private copyTimeout: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.loadUrls();
    this.loadTags();
  }

  ngOnDestroy(): void {
    if (this.copyTimeout) clearTimeout(this.copyTimeout);
  }

  protected loadUrls(): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.urlService.getUserUrls(this.selectedTagFilter() ?? undefined).subscribe({
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

  protected loadTags(): void {
    this.tagService.getTags().subscribe({
      next: (tags) => this.userTags.set(tags),
      error: () => {}
    });
  }

  protected onTagFilterChange(tagId: number | null): void {
    this.selectedTagFilter.set(tagId);
    this.loadUrls();
  }

  protected onCopy(shortUrl: string, shortCode: string): void {
    navigator.clipboard.writeText(shortUrl);
    this.toastService.show('Copied to clipboard!', 'success');
    if (this.copyTimeout) clearTimeout(this.copyTimeout);
    this.copiedCode.set(shortCode);
    this.copyTimeout = setTimeout(() => this.copiedCode.set(null), 2000);
  }

  protected onDelete(shortCode: string): void {
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

  protected onToggleStatus(url: UserUrlResponse): void {
    this.togglingCode.set(url.shortCode);
    const newActive = !url.isActive;
    this.urlService.toggleUrlStatus(url.shortCode, newActive).subscribe({
      next: () => {
        this.urls.update(list =>
          list.map(u => u.shortCode === url.shortCode ? { ...u, isActive: newActive } : u)
        );
        this.togglingCode.set(null);
        this.toastService.show(newActive ? 'Link reactivated' : 'Link paused', 'success');
      },
      error: () => {
        this.togglingCode.set(null);
        this.toastService.show('Failed to update link status', 'error');
      }
    });
  }

  protected onOpenEdit(url: UserUrlResponse): void {
    this.editingUrl.set(url);
  }

  protected onEditSaved(updated: UserUrlResponse): void {
    this.urls.update(list =>
      list.map(u => u.shortCode === updated.shortCode ? updated : u)
    );
    this.editingUrl.set(null);
    this.toastService.show('Link updated', 'success');
  }

  protected onEditClosed(): void {
    this.editingUrl.set(null);
  }

  protected onLogout(): void {
    this.authService.logout();
  }

  protected formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  }

  protected formatExpiry(iso: string): string {
    const d = new Date(iso);
    const now = new Date();
    const diff = d.getTime() - now.getTime();
    if (diff < 0) return 'Expired';
    const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
    if (days === 1) return 'Expires tomorrow';
    if (days <= 7) return `Expires in ${days} days`;
    return `Expires ${d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}`;
  }
}
