import { Component, input, output, signal, inject, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Check, LucideAngularModule, X } from 'lucide-angular';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { TagResponse, UrlShortenerService, UserUrlResponse } from '../../services/url-shortener.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ModalComponent } from '../../shared/modal/modal';

@Component({
  selector: 'app-edit-link-modal',
  standalone: true,
  imports: [FormsModule, LucideAngularModule, ModalComponent],
  templateUrl: './edit-link-modal.html',
  styleUrl: './edit-link-modal.css'
})
export class EditLinkModalComponent implements OnChanges, OnDestroy {
  readonly url = input.required<UserUrlResponse | null>();
  readonly userTags = input.required<TagResponse[]>();

  readonly saved = output<UserUrlResponse>();
  readonly closed = output<void>();

  protected readonly checkIcon = Check;
  protected readonly xIcon = X;

  protected readonly editAlias = signal('');
  protected readonly editExpiresAt = signal('');
  protected readonly editClearExpiration = signal(false);
  protected readonly editPassword = signal('');
  protected readonly editClearPassword = signal(false);
  protected readonly editSelectedTagIds = signal<Set<number>>(new Set());
  protected readonly editSaving = signal(false);
  protected readonly editAliasAvailable = signal<boolean | null>(null);
  protected readonly editAliasChecking = signal(false);

  private readonly urlService = inject(UrlShortenerService);
  private readonly toastService = inject(ToastService);
  private readonly aliasSubject = new Subject<string>();
  private sub?: Subscription;

  get isOpen(): boolean {
    return this.url() != null;
  }

  get editMinDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  get editAliasValidationMessage(): string | null {
    const alias = this.editAlias();
    if (!alias) return null;
    if (!this.isAliasFormatValid(alias)) {
      return 'Must be 3–30 characters: letters, numbers, hyphens, underscores only';
    }
    return null;
  }

  constructor() {
    this.sub = this.aliasSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(alias => {
        const currentUrl = this.url();
        if (!alias || !this.isAliasFormatValid(alias)) {
          this.editAliasAvailable.set(null);
          this.editAliasChecking.set(false);
          return [];
        }
        if (currentUrl && alias === currentUrl.shortCode) {
          this.editAliasAvailable.set(true);
          this.editAliasChecking.set(false);
          return [];
        }
        this.editAliasChecking.set(true);
        return this.urlService.checkAliasAvailability(alias);
      })
    ).subscribe({
      next: (result) => {
        this.editAliasChecking.set(false);
        this.editAliasAvailable.set(result.available);
      },
      error: () => {
        this.editAliasChecking.set(false);
        this.editAliasAvailable.set(null);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const url = this.url();
    if (url && changes['url']) {
      this.editAlias.set(url.isCustomAlias ? url.shortCode : '');
      this.editExpiresAt.set(url.expiresAt ? url.expiresAt.split('T')[0] : '');
      this.editClearExpiration.set(false);
      this.editPassword.set('');
      this.editClearPassword.set(false);
      this.editSelectedTagIds.set(new Set(url.tags.map(t => t.id)));
      this.editAliasAvailable.set(null);
      this.editAliasChecking.set(false);
    }
  }

  protected onClose(): void {
    this.closed.emit();
  }

  protected onEditAliasChange(value: string): void {
    this.editAlias.set(value);
    this.editAliasAvailable.set(null);
    if (value) this.aliasSubject.next(value);
  }

  protected isAliasFormatValid(alias: string): boolean {
    return /^[a-zA-Z0-9_-]{3,30}$/.test(alias);
  }

  protected toggleTag(tagId: number): void {
    this.editSelectedTagIds.update(set => {
      const next = new Set(set);
      if (next.has(tagId)) next.delete(tagId);
      else next.add(tagId);
      return next;
    });
  }

  protected onSave(): void {
    const url = this.url();
    if (!url) return;

    const alias = this.editAlias();
    if (alias && !this.isAliasFormatValid(alias)) {
      this.toastService.show('Invalid alias format', 'error');
      return;
    }
    if (alias && alias !== url.shortCode && this.editAliasAvailable() === false) {
      this.toastService.show('This alias is already taken', 'error');
      return;
    }

    this.editSaving.set(true);

    const expiresAtValue = this.editExpiresAt();
    const clearExpiration = this.editClearExpiration();

    const request: {
      customAlias?: string;
      expiresAt?: string;
      clearExpiration?: boolean;
      tagIds: number[];
      password?: string;
      clearPassword?: boolean;
    } = {
      tagIds: Array.from(this.editSelectedTagIds())
    };
    if (alias && alias !== url.shortCode) request.customAlias = alias;
    if (clearExpiration) request.clearExpiration = true;
    else if (expiresAtValue) request.expiresAt = new Date(expiresAtValue).toISOString();
    if (this.editClearPassword()) request.clearPassword = true;
    else {
      const pwd = this.editPassword()?.trim();
      if (pwd) request.password = pwd;
    }

    this.urlService.updateUrl(url.shortCode, request).subscribe({
      next: (updated) => {
        this.editSaving.set(false);
        this.saved.emit(updated);
      },
      error: (err) => {
        this.editSaving.set(false);
        this.toastService.show(err.error?.message || 'Failed to update link', 'error');
      }
    });
  }
}
