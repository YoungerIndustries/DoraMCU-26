import { Directive, ElementRef, Output, EventEmitter, HostListener } from '@angular/core';

@Directive({
  selector: '[appPullToRefresh]',
  standalone: true,
})
export class PullToRefreshDirective {
  @Output() refreshed = new EventEmitter<void>();

  private startY = 0;
  private currentY = 0;
  private isDragging = false;
  private threshold = 100; // pixels to trigger refresh

  constructor(private el: ElementRef) {}

  @HostListener('touchstart', ['$event'])
  onTouchStart(event: TouchEvent): void {
    if (window.scrollY === 0) {
      this.startY = event.touches[0].clientY;
      this.isDragging = true;
    }
  }

  @HostListener('touchmove', ['$event'])
  onTouchMove(event: TouchEvent): void {
    if (!this.isDragging) return;

    this.currentY = event.touches[0].clientY - this.startY;
    if (this.currentY > 0) {
      event.preventDefault();
      this.el.nativeElement.style.transform = `translateY(${Math.min(this.currentY, this.threshold * 1.5)}px)`;
      this.el.nativeElement.style.transition = 'none';
    }
  }

  @HostListener('touchend')
  onTouchEnd(): void {
    if (!this.isDragging) return;

    this.isDragging = false;
    this.el.nativeElement.style.transition = 'transform 0.3s ease';

    if (this.currentY > this.threshold) {
      this.refreshed.emit();
    }

    setTimeout(() => {
      this.el.nativeElement.style.transform = 'translateY(0)';
      this.currentY = 0;
    }, 300);
  }
}
