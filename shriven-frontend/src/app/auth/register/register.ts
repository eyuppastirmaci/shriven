import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Link2, Lock, LucideAngularModule, Mail, UserPlus } from 'lucide-angular';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink, LucideAngularModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  protected readonly logoIcon = Link2;
  protected readonly mailIcon = Mail;
  protected readonly lockIcon = Lock;
  protected readonly registerIcon = UserPlus;

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly confirmPassword = signal('');
  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);

  get passwordsMatch(): boolean {
    return this.password() === this.confirmPassword();
  }

  onSubmit(): void {
    if (!this.email() || !this.password()) return;

    if (!this.passwordsMatch) {
      this.error.set('Passwords do not match');
      return;
    }

    if (this.password().length < 8) {
      this.error.set('Password must be at least 8 characters');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    this.authService.register({ email: this.email(), password: this.password() }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error.set(err.error?.message || 'Registration failed. Please try again.');
        this.isLoading.set(false);
      }
    });
  }
}
