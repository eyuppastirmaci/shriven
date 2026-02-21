import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Footer } from './footer/footer';
import { Toast } from './shared/toast/toast';
import { ThemeToggle } from './shared/theme-toggle/theme-toggle';
import { Sidebar } from './sidebar/sidebar';
import { AuthService } from './services/auth.service';
import { SidebarService } from './sidebar/sidebar.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Footer, Toast, ThemeToggle, Sidebar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly authService = inject(AuthService);
  protected readonly sidebarService = inject(SidebarService);
}
