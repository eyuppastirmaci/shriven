import { Component, inject } from '@angular/core';
import { LucideAngularModule, X } from 'lucide-angular';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './toast.html',
  styleUrl: './toast.css'
})
export class Toast {
  protected readonly toastService = inject(ToastService);
  protected readonly xIcon = X;
}
