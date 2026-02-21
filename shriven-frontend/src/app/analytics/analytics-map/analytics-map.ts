import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  inject,
  signal
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AnalyticsService, GeoStatItem } from '../../services/analytics.service';
import { getCentroid } from '../country-centroids';
// Leaflet is loaded dynamically in the browser to avoid SSR issues
type LeafletMap = import('leaflet').Map;
type LeafletCircleMarker = import('leaflet').CircleMarker;

@Component({
  selector: 'app-analytics-map',
  standalone: true,
  templateUrl: './analytics-map.html',
  styleUrl: './analytics-map.css'
})
export class AnalyticsMapComponent implements OnInit, OnChanges, OnDestroy {
  @Input({ required: true }) shortCode!: string;

  private readonly analyticsService = inject(AnalyticsService);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  protected readonly geoStats = signal<GeoStatItem[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  private map: LeafletMap | null = null;
  private markers: LeafletCircleMarker[] = [];

  private readonly CARTODB_DARK =
    'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png';

  ngOnInit(): void {
    this.loadGeoStats();
  }

  ngOnChanges(): void {
    if (this.shortCode) {
      this.destroyMap();
      this.loadGeoStats();
    }
  }

  ngOnDestroy(): void {
    this.destroyMap();
  }

  private loadGeoStats(): void {
    if (!this.shortCode) return;
    this.loading.set(true);
    this.error.set(null);
    this.analyticsService.getGeoStats(this.shortCode).subscribe({
      next: (list) => {
        // Only show known countries on the map; hide Unknown (e.g. localhost/private IPs)
        this.geoStats.set(
          list.filter((g) => g.count > 0 && g.countryCode !== 'Unknown')
        );
        this.loading.set(false);
        if (this.isBrowser && this.geoStats().length > 0) {
          setTimeout(() => this.initMap(), 0);
        }
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Failed to load map data');
        this.loading.set(false);
      }
    });
  }

  private async initMap(): Promise<void> {
    if (typeof window === 'undefined') return;
    const container = document.getElementById('analytics-map-container');
    if (!container || this.map) return;
    const L = await import('leaflet');
    this.map = L.map(container, {
      center: [20, 0],
      zoom: 2,
      zoomControl: true
    });
    L.control.zoom({ position: 'topright' }).addTo(this.map!);
    L.tileLayer(this.CARTODB_DARK, {
      attribution: '&copy; <a href="https://carto.com/">CARTO</a>',
      maxZoom: 19
    }).addTo(this.map!);

    const stats = this.geoStats();
    const maxCount = Math.max(...stats.map((s) => s.count), 1);
    const minRadius = 4;
    const maxRadius = 14;

    stats.forEach((item) => {
      const [lat, lng] = getCentroid(item.countryCode);
      const radius =
        maxCount > 1
          ? minRadius + ((item.count / maxCount) * (maxRadius - minRadius))
          : maxRadius;
      const marker = L.circleMarker([lat, lng], {
        radius,
        fillColor: 'var(--accent)',
        color: 'var(--accent-soft)',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.8
      });
      const name = item.countryName ?? item.countryCode;
      marker.bindTooltip(`${name} – ${item.count} click${item.count !== 1 ? 's' : ''}`, {
        className: 'analytics-map-tooltip',
        direction: 'top',
        offset: [0, -radius]
      });
      marker.addTo(this.map!);
      this.markers.push(marker);
    });
  }

  private destroyMap(): void {
    this.markers.forEach((m) => m.remove());
    this.markers = [];
    this.map?.remove();
    this.map = null;
  }
}
