import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  BarChart2,
  ChevronLeft,
  ChevronRight,
  LayoutDashboard,
  Link2,
  LogOut,
  LucideAngularModule,
  Plus,
} from 'lucide-angular';
import { AuthService } from '../services/auth.service';
import { SidebarIcon } from '../shared/sidebar-icon/sidebar-icon';
import { TooltipDirective } from '../shared/tooltip/tooltip.directive';
import { SidebarService } from './sidebar.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [LucideAngularModule, RouterLink, RouterLinkActive, SidebarIcon, TooltipDirective],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  protected readonly authService = inject(AuthService);
  protected readonly sidebarService = inject(SidebarService);

  protected readonly dashboardIcon = LayoutDashboard;
  protected readonly analyticsIcon = BarChart2;
  protected readonly logoutIcon = LogOut;
  protected readonly newLinkIcon = Plus;
  protected readonly logoIcon = Link2;
  protected readonly collapseIcon = ChevronLeft;
  protected readonly expandIcon = ChevronRight;

  protected readonly collapsed = this.sidebarService.collapsed;
  protected readonly userMenuOpen = signal(false);

  protected readonly username = computed(() => {
    const email = this.authService.currentEmail();
    if (!email) return null;
    const localPart = email.split('@')[0]?.trim();
    return localPart || email;
  });

  protected readonly avatarInitial = computed(() => {
    const name = this.username();
    if (!name) return '?';
    return name.charAt(0).toUpperCase();
  });

  protected toggleCollapse(): void {
    this.sidebarService.toggle();
    this.userMenuOpen.set(false);
  }

  protected toggleUserMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.userMenuOpen.update(v => !v);
  }

  protected logout(event: MouseEvent): void {
    event.stopPropagation();
    this.userMenuOpen.set(false);
    this.authService.logout();
  }

  @HostListener('document:click')
  protected onDocumentClick(): void {
    this.userMenuOpen.set(false);
  }
}
