import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Link2, Lock, LogIn, LucideAngularModule, Mail } from 'lucide-angular';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, LucideAngularModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  protected readonly logoIcon = Link2;
  protected readonly mailIcon = Mail;
  protected readonly lockIcon = Lock;
  protected readonly loginIcon = LogIn;

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);

  onSubmit(): void {
    if (!this.email() || !this.password()) return;

    this.isLoading.set(true);
    this.error.set(null);

    this.authService.login({ email: this.email(), password: this.password() }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error.set(err.error?.message || 'Invalid email or password');
        this.isLoading.set(false);
      }
    });
  }
}
