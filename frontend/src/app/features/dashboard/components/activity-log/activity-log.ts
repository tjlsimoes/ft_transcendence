import { Component, input } from '@angular/core';

export type ActivityLevel = 'success' | 'warning' | 'info';

export interface ActivityLogItem {
  time: string;
  level: ActivityLevel;
  title: string;
  message: string;
}

@Component({
  selector: 'app-activity-log',
  templateUrl: './activity-log.html',
  styleUrl: './activity-log.css',
})
export class ActivityLog {
  logs = input.required<ActivityLogItem[]>();
}
