import { Component, Input } from '@angular/core';
import { LucideAngularModule, icons } from 'lucide-angular';

@Component({
  selector: 'app-sidebar-icon',
  standalone: true,
  imports: [LucideAngularModule],
  template: `
    <span class="sidebar-icon-wrap">
      <lucide-icon [img]="icon" [size]="iconSize" />
      @if (!collapsed) {
        <span class="sidebar-icon-label">{{ label }}</span>
      }
    </span>
  `,
  styles: [`
    .sidebar-icon-wrap {
      display: inline-flex;
      align-items: center;
      gap: 0.75rem;
      width: 100%;
    }

    .sidebar-icon-label {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  `]
})
export class SidebarIcon {
  @Input({ required: true }) icon!: typeof icons[keyof typeof icons];
  @Input({ required: true }) label = '';
  @Input() collapsed = false;
  @Input() iconSize = 18;
}
