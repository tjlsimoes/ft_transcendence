import { Component, signal, computed, ElementRef, ViewChild, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ChallengeService } from '../core/services/challenge.service';
import { ArenaTimer } from './arena-timer';
import { CodeEditorComponent, EditorTheme } from './code-editor/code-editor';
import {
  SubmissionResult,
  TestCaseResult,
  RunResult,
  mockWrongAnswerResult,
  mockCorrectAnswerResult,
  mockRunSuccess,
  mockRunCompileError,
} from './submission-result.model';

export type PanelTab = 'Problem' | 'Submissions';

@Component({
  selector: 'app-arena-page',
  imports: [FormsModule, NgClass, ArenaTimer, CodeEditorComponent],
  templateUrl: './arena-page.html',
  styleUrl: './arena-page.css',
})
export class ArenaPage implements OnInit, OnDestroy {
  @ViewChild('arenaShell') arenaShell!: ElementRef<HTMLElement>;
  @ViewChild('editorPanel') editorPanel!: ElementRef<HTMLElement>;
  @ViewChild(ArenaTimer) arenaTimer!: ArenaTimer;

  private route = inject(ActivatedRoute);
  private challengeService = inject(ChallengeService);

  // ── Contexto do Duel (recebido via query params do matchmaking) ───
  readonly duelId = signal<number | null>(null);
  readonly challengeId = signal<number | null>(null);
  readonly opponentName = signal<string | null>(null);

  // ── Dados do Challenge ───
  readonly challengeTitle = signal<string>('Loading challenge...');
  readonly challengeDescription = signal<string>('Please wait while we fetch the problem details...');
  readonly formattedDescription = computed(() => {
    // Replace literal '\n' string with HTML line breaks
    return this.challengeDescription().replace(/\\n/g, '<br>');
  });

  readonly panelTabs: PanelTab[] = ['Problem', 'Submissions'];
  activeTab = signal<PanelTab>('Problem');

  readonly languages = ['C', 'C++', 'Java', 'Python 3', 'JavaScript'];
  selectedLanguage = signal('C');

  readonly themes: EditorTheme[] = ['Dark', 'Light'];
  selectedTheme = signal<EditorTheme>('Dark');

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

  ngOnInit(): void {
    const params = this.route.snapshot.queryParams;
    if (params['duelId']) {
      this.duelId.set(Number(params['duelId']));
    }
    if (params['challengeId']) {
      this.challengeId.set(Number(params['challengeId']));
    }
    if (params['opponent']) {
      this.opponentName.set(params['opponent']);
    }

    console.log('Arena loaded — duelId:', this.duelId(), 'challengeId:', this.challengeId(), 'opponent:', this.opponentName());

    const cid = this.challengeId();
    if (cid) {
      this.challengeService.getChallenge(cid).subscribe({
        next: (challenge) => {
          this.challengeTitle.set(challenge.title);
          this.challengeDescription.set(challenge.description);
        },
        error: (err) => {
          console.error('Failed to load challenge details:', err);
          this.challengeTitle.set('Error loading challenge');
          this.challengeDescription.set('Could not fetch the problem details from the server.');
        }
      });
    }
  }

  private resizing = false;
  private startX = 0;
  private startWidth = 0;
  private readonly MIN_WIDTH = 250;
  private readonly MAX_WIDTH = 750;
  private boundMouseMove = this.onMouseMove.bind(this);
  private boundMouseUp   = this.stopResize.bind(this);

  // ── Footer vertical resize ──────────────────────────────────────────
  private footerResizing = false;
  private footerStartY = 0;
  private footerStartHeight = 0;
  private readonly MIN_FOOTER_HEIGHT = 55;
  private readonly MAX_FOOTER_HEIGHT = 600;
  private boundFooterMouseMove = this.onFooterMouseMove.bind(this);
  private boundFooterMouseUp   = this.stopFooterResize.bind(this);

  startFooterResize(e: MouseEvent): void {
    this.footerResizing = true;
    this.footerStartY = e.clientY;
    const footer = this.editorPanel.nativeElement.querySelector('.editor-footer') as HTMLElement;
    this.footerStartHeight = footer.getBoundingClientRect().height;
    document.addEventListener('mousemove', this.boundFooterMouseMove);
    document.addEventListener('mouseup',   this.boundFooterMouseUp);
    e.preventDefault();
  }

  private onFooterMouseMove(e: MouseEvent): void {
    if (!this.footerResizing) return;
    const delta = this.footerStartY - e.clientY;
    const newHeight = Math.min(this.MAX_FOOTER_HEIGHT, Math.max(this.MIN_FOOTER_HEIGHT, this.footerStartHeight + delta));
    this.editorPanel.nativeElement.style.setProperty('--footer-height', `${newHeight}px`);
  }

  private stopFooterResize(): void {
    this.footerResizing = false;
    document.removeEventListener('mousemove', this.boundFooterMouseMove);
    document.removeEventListener('mouseup',   this.boundFooterMouseUp);
  }

  startResize(e: MouseEvent): void {
    this.resizing = true;
    this.startX = e.clientX;
    const raw = getComputedStyle(this.arenaShell.nativeElement).getPropertyValue('--panel-width');
    this.startWidth = parseInt(raw) || 400;
    document.addEventListener('mousemove', this.boundMouseMove);
    document.addEventListener('mouseup',   this.boundMouseUp);
    e.preventDefault();
  }

  private onMouseMove(e: MouseEvent): void {
    if (!this.resizing) return;
    const newWidth = Math.min(this.MAX_WIDTH, Math.max(this.MIN_WIDTH, this.startWidth + e.clientX - this.startX));
    this.arenaShell.nativeElement.style.setProperty('--panel-width', `${newWidth}px`);
  }

  private stopResize(): void {
    this.resizing = false;
    document.removeEventListener('mousemove', this.boundMouseMove);
    document.removeEventListener('mouseup',   this.boundMouseUp);
  }

  ngOnDestroy(): void {
    this.stopResize();
    this.stopFooterResize();
    if (this.notifTimeoutId !== null) clearTimeout(this.notifTimeoutId);
  }

  setTab(tab: PanelTab): void {
    this.activeTab.set(tab);
  }

  toggleTestInput(): void {
    this.showTestInput.set(!this.showTestInput());
  }

  runCode(): void {
    // TODO: replace with real backend call;
    // map HTTP response to RunResult and call:
    const result = mockRunSuccess(); // swap to mockRunCompileError() to test
    this.runResult.set(result);
    this.runPanelOpen.set(true);
    // Close submit panel to avoid overlap
    this.closeResultPanel();
    console.log('Running code:', this.code());
  }

  // ── Run result panel ─────────────────────────────────────────────────

  /** Current run result; null = panel hidden. */
  runResult = signal<RunResult | null>(null);

  /** Whether the run result panel body is expanded. */
  runPanelOpen = signal(false);

  toggleRunPanel(): void {
    this.runPanelOpen.set(!this.runPanelOpen());
  }

  closeRunPanel(): void {
    this.runResult.set(null);
    this.runPanelOpen.set(false);
  }

  // ── Submission result panel ────────────────────────────────────────────

  /** Current submission result; null = no result yet / panel hidden. */
  submissionResult = signal<SubmissionResult | null>(null);

  /** The test-case row currently expanded in the result panel. */
  activeTestCase = signal<TestCaseResult | null>(null);

  /** Whether the result panel is expanded (true) or collapsed (false). */
  resultPanelOpen = signal(false);

  readonly resultPanelHeight = computed(() => this.resultPanelOpen() ? '260px' : '0px');

  openResultCase(tc: TestCaseResult): void {
    this.activeTestCase.set(tc);
  }

  toggleResultPanel(): void {
    this.resultPanelOpen.set(!this.resultPanelOpen());
  }

  closeResultPanel(): void {
    this.submissionResult.set(null);
    this.resultPanelOpen.set(false);
    this.activeTestCase.set(null);
  }

  submitCode(): void {
    // TODO: replace with real backend call;
    // map HTTP response to SubmissionResult using the model and set the signal.
    const result = mockWrongAnswerResult(); // swap to mockCorrectAnswerResult() to test
    this.submissionResult.set(result);
    this.activeTestCase.set(result.testCases[0] ?? null);
    this.resultPanelOpen.set(true);
    // Close run panel to avoid overlap
    this.closeRunPanel();
    console.log('Submitting code:', this.code());
  }

  /** Called by the timer when it reaches zero — auto-submits the code. */
  onTimeUp(): void {
    this.submitCode();
  }

  /** Whether the opponent-finished notification banner is visible. */
  showOpponentNotif = signal(false);

  private notifTimeoutId: ReturnType<typeof setTimeout> | null = null;

  /**
   * Call this when the backend signals the opponent submitted a correct solution.
   * Shows a notification banner and forwards the event to the timer.
   */
  opponentFinished(): void {
    this.arenaTimer?.opponentFinished();
    this.showOpponentNotif.set(true);
    if (this.notifTimeoutId !== null) clearTimeout(this.notifTimeoutId);
    this.notifTimeoutId = setTimeout(() => this.showOpponentNotif.set(false), 5000);
  }

  dismissOpponentNotif(): void {
    this.showOpponentNotif.set(false);
    if (this.notifTimeoutId !== null) {
      clearTimeout(this.notifTimeoutId);
      this.notifTimeoutId = null;
    }
  }

  /**
   * Mock: simulates the opponent finishing.
   * Call this from the template (e.g. a dev button) or from a WebSocket handler.
   */
  mockOpponentFinished(): void {
    this.opponentFinished();
  }
}
