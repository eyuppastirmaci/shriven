import { Injectable, inject } from '@angular/core';
import QRCode from 'qrcode';
import { ToastService } from '../shared/toast/toast.service';

const QR_SIZE = 300;

@Injectable({ providedIn: 'root' })
export class QrCodeService {
  private readonly toastService = inject(ToastService);

  /**
   * Generates a QR code for the given short URL and triggers a PNG download.
   * @param shortUrl The full short URL to encode (e.g. https://example.com/abc123)
   * @param shortCode Optional short code used for the filename (e.g. abc123 -> qr-abc123.png)
   */
  downloadQr(shortUrl: string, shortCode?: string): void {
    const filename = shortCode ? `qr-${shortCode}.png` : 'qr-code.png';

    QRCode.toDataURL(shortUrl, {
      width: QR_SIZE,
      margin: 2,
      type: 'image/png'
    })
      .then((dataUrl: string) => {
        const link = document.createElement('a');
        link.href = dataUrl;
        link.download = filename;
        link.click();
        this.toastService.show('QR code downloaded', 'success');
      })
      .catch(() => {
        this.toastService.show('Failed to generate QR code', 'error');
      });
  }
}
