import { Directive, ElementRef, Input, OnDestroy, OnInit, Renderer2, inject } from '@angular/core';

@Directive({
  selector: '[appTooltip]',
  standalone: true,
})
export class TooltipDirective implements OnInit, OnDestroy {
  @Input('appTooltip') text = '';
  @Input() tooltipPosition: 'right' | 'top' | 'bottom' = 'right';
  @Input() tooltipDisabled = false;

  private readonly el = inject(ElementRef);
  private readonly renderer = inject(Renderer2);

  private tip: HTMLElement | null = null;
  private showFn = () => this.show();
  private hideFn = () => this.hide();

  ngOnInit(): void {
    this.renderer.setAttribute(this.el.nativeElement, 'data-tooltip-host', '');
    this.el.nativeElement.addEventListener('mouseenter', this.showFn);
    this.el.nativeElement.addEventListener('mouseleave', this.hideFn);
    this.el.nativeElement.addEventListener('click', this.hideFn);
  }

  ngOnDestroy(): void {
    this.hide();
    this.el.nativeElement.removeEventListener('mouseenter', this.showFn);
    this.el.nativeElement.removeEventListener('mouseleave', this.hideFn);
    this.el.nativeElement.removeEventListener('click', this.hideFn);
  }

  private show(): void {
    if (this.tooltipDisabled || !this.text) return;

    this.tip = this.renderer.createElement('div');
    this.renderer.addClass(this.tip, 'app-tooltip');
    this.renderer.addClass(this.tip, `app-tooltip--${this.tooltipPosition}`);
    this.renderer.setProperty(this.tip, 'textContent', this.text);
    this.renderer.appendChild(document.body, this.tip);

    const rect = this.el.nativeElement.getBoundingClientRect();
    const tip = this.tip!;

    requestAnimationFrame(() => {
      const tw = tip.offsetWidth;
      const th = tip.offsetHeight;
      let top = 0;
      let left = 0;

      if (this.tooltipPosition === 'right') {
        top = rect.top + rect.height / 2 - th / 2;
        left = rect.right + 10;
      } else if (this.tooltipPosition === 'top') {
        top = rect.top - th - 8;
        left = rect.left + rect.width / 2 - tw / 2;
      } else {
        top = rect.bottom + 8;
        left = rect.left + rect.width / 2 - tw / 2;
      }

      this.renderer.setStyle(tip, 'top', `${top + window.scrollY}px`);
      this.renderer.setStyle(tip, 'left', `${left + window.scrollX}px`);
      this.renderer.addClass(tip, 'app-tooltip--visible');
    });
  }

  private hide(): void {
    if (this.tip) {
      this.renderer.removeChild(document.body, this.tip);
      this.tip = null;
    }
  }
}
