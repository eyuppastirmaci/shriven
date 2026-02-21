import { Component, inject } from '@angular/core';
import { Monitor, Moon, Sun, LucideIconData } from 'lucide-angular';
import { Dropdown, DropdownItem } from '../dropdown/dropdown';
import { ThemeService, Theme } from '../../services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [Dropdown],
  template: `
    <app-dropdown
      [items]="items"
      [selected]="themeService.theme()"
      (selectionChange)="onSelect($event)"
    />
  `,
})
export class ThemeToggle {
  protected readonly themeService = inject(ThemeService);

  protected readonly items: DropdownItem[] = [
    { value: 'light',  label: 'Light',  icon: Sun     as LucideIconData },
    { value: 'dark',   label: 'Dark',   icon: Moon    as LucideIconData },
    { value: 'system', label: 'System', icon: Monitor as LucideIconData },
  ];

  protected onSelect(value: string): void {
    this.themeService.setTheme(value as Theme);
  }
}
