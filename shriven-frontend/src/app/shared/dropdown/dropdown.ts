import {
  Component,
  ElementRef,
  HostListener,
  Input,
  Output,
  EventEmitter,
  computed,
  inject,
  signal,
} from '@angular/core';
import { Check, ChevronDown, LucideAngularModule, LucideIconData } from 'lucide-angular';

export interface DropdownItem {
  value: string;
  label: string;
  icon: LucideIconData;
}

@Component({
  selector: 'app-dropdown',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './dropdown.html',
  styleUrl: './dropdown.css',
})
export class Dropdown {
  @Input() items: DropdownItem[] = [];
  @Input() selected = '';
  @Input() placeholder = 'Select...';
  @Output() selectionChange = new EventEmitter<string>();

  protected readonly chevronIcon = ChevronDown;
  protected readonly checkIcon = Check;

  protected readonly isOpen = signal(false);

  private readonly el = inject(ElementRef);

  protected readonly selectedItem = computed(() =>
    this.items.find((i) => i.value === this.selected),
  );

  @HostListener('document:click', ['$event.target'])
  onDocumentClick(target: EventTarget | null): void {
    if (!this.el.nativeElement.contains(target)) {
      this.isOpen.set(false);
    }
  }

  protected toggle(): void {
    this.isOpen.update((v) => !v);
  }

  protected select(value: string): void {
    this.selectionChange.emit(value);
    this.isOpen.set(false);
  }
}
