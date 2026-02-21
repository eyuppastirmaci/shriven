import { Component, input, output } from '@angular/core';
import { LucideAngularModule, X } from 'lucide-angular';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './modal.html',
  styleUrl: './modal.css'
})
export class ModalComponent {
  readonly title = input.required<string>();
  readonly isOpen = input.required<boolean>();
  readonly closeLabel = input<string>('Close');

  readonly close = output<void>();

  protected readonly xIcon = X;

  onBackdropClick(): void {
    this.close.emit();
  }

  onCloseClick(): void {
    this.close.emit();
  }

  onContentClick(event: Event): void {
    event.stopPropagation();
  }
}
