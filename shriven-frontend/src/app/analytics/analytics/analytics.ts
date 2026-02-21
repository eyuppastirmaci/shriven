import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ArrowLeft, LucideAngularModule } from 'lucide-angular';
import { AnalyticsLinkSelectorComponent } from '../analytics-link-selector/analytics-link-selector';
import { AnalyticsMapComponent } from '../analytics-map/analytics-map';
import { AnalyticsDeviceBreakdownComponent } from '../analytics-device-breakdown/analytics-device-breakdown';
import { AnalyticsReferrerBreakdownComponent } from '../analytics-referrer-breakdown/analytics-referrer-breakdown';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [
    RouterLink,
    LucideAngularModule,
    AnalyticsLinkSelectorComponent,
    AnalyticsMapComponent,
    AnalyticsDeviceBreakdownComponent,
    AnalyticsReferrerBreakdownComponent
  ],
  templateUrl: './analytics.html',
  styleUrl: './analytics.css'
})
export class Analytics {
  protected readonly arrowLeftIcon = ArrowLeft;
  protected readonly selectedShortCode = signal<string | null>(null);

  onShortCodeChange(shortCode: string): void {
    this.selectedShortCode.set(shortCode);
  }
}
