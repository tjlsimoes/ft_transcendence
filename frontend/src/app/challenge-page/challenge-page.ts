import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { Navbar } from '../shared/components/navbar/navbar';

export type PanelTab = 'Problem' | 'Submissions' | 'Leaderboard' | 'Discussions' | 'Editorial';

@Component({
  selector: 'app-challenge-page',
  imports: [FormsModule, NgClass, Navbar],
  templateUrl: './challenge-page.html',
  styleUrl: './challenge-page.css',
})
export class ChallengePage {
  readonly panelTabs: PanelTab[] = ['Problem', 'Submissions', 'Leaderboard', 'Discussions', 'Editorial'];
  activeTab = signal<PanelTab>('Problem');

  readonly languages = ['C', 'C++', 'Java', 'Python 3', 'JavaScript'];
  selectedLanguage = signal('C');

  readonly themes = ['Dark', 'Light'];
  selectedTheme = signal('Dark');

  code = signal(
`#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>

int main() {

    /* Enter your code here. Read input from STDIN. Print output to STDOUT */
    return 0;
}`);

  testInput = signal('');
  showTestInput = signal(false);

  setTab(tab: PanelTab): void {
    this.activeTab.set(tab);
  }

  toggleTestInput(): void {
    this.showTestInput.set(!this.showTestInput());
  }

  runCode(): void {
    // TODO: integrate with backend runner
    console.log('Running code:', this.code());
  }

  submitCode(): void {
    // TODO: integrate with backend submission
    console.log('Submitting code:', this.code());
  }
}
