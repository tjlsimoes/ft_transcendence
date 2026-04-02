import { Component, input } from '@angular/core';

export interface StatusBarData {
  server: string;
  onlineStatus: string;
  latency: string;
  build: string;
  copyright: string;
}

@Component({
  selector: 'app-status-bar',
  templateUrl: './status-bar.html',
  styleUrl: './status-bar.css',
})
export class StatusBar {
  status = input.required<StatusBarData>();
}
