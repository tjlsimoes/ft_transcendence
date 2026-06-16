import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-modal',
  imports: [],
  templateUrl: './modal.html',
  styleUrl: './modal.css',
})
export class Modal {
  open = input.required<boolean>();
  closed = output<void>();

  onBackdropClick(): void {
    this.closed.emit();
  }
}
