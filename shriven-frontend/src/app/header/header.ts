import { Component } from '@angular/core';
import { LucideAngularModule, Link2 } from 'lucide-angular';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  protected readonly linkIcon = Link2;
}
