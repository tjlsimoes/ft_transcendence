import { Component, signal, computed, ElementRef, ViewChild, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { Router } from '@angular/router';
import { ChallengeService } from '../core/services/challenge.service';
import { DuelService } from '../core/services/duel.service';
import { WebSocketService } from '../core/services/websocket.service';
import { AuthService } from '../core/services/auth.service';
import { UserService } from '../core/services/user.service';
import { Subscription } from 'rxjs';
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


  private router = inject(Router);
  private challengeService = inject(ChallengeService);
  private duelService = inject(DuelService);
  private wsService = inject(WebSocketService);
  private authService = inject(AuthService);
  private userService = inject(UserService);

  private subs = new Subscription();

  // ── Contexto do Duel (carregado do backend via getActiveDuel) ───
  readonly duelId = signal<number | null>(null);
  readonly challengeId = signal<number | null>(null);
  readonly opponentName = signal<string | null>(null);

  /**
   * Indica se o duel está em curso (IN_PROGRESS).
   * Usado pelo arenaDeactivateGuard para bloquear navegação durante o duel.
   * Fica false quando o duel termina (COMPLETED, DRAW) ou quando não foi iniciado.
   */
  readonly isDuelActive = signal<boolean>(false);

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
    const token = this.authService.getToken();
    if (token) {
      this.wsService.connect(token);
    }

    if (!this.userService.currentUser()) {
      this.userService.loadMe().subscribe();
    }

    // ── Carregar contexto do duel inteiramente do backend ───────────
    // Sem query params — o challengeId vem do duel record na DB,
    // tornando impossível manipular o URL para ver outro challenge.
    this.duelService.getActiveDuel().subscribe({
      next: (activeDuel) => {
        this.duelId.set(activeDuel.duelId);
        this.challengeId.set(activeDuel.challengeId);
        this.opponentName.set(activeDuel.opponentName);

        console.log('Arena loaded — duelId:', activeDuel.duelId,
          'challengeId:', activeDuel.challengeId, 'opponent:', activeDuel.opponentName);

        // Carregar o challenge a partir do challengeId do duel (backend)
        this.challengeService.getChallenge(activeDuel.challengeId).subscribe({
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

        // Subscrever ao WebSocket do duel
        this.subs.add(
          this.wsService.subscribe<any>(`/topic/duel/${activeDuel.duelId}`).subscribe((event) => {
            this.handleDuelEvent(event);
          })
        );

        // Sync initial state on load/refresh
        this.duelService.getDuelStatus(activeDuel.duelId).subscribe({
          next: (status) => {
            console.log('Initial duel status sync:', status);
            if (this.arenaTimer && status.timeLeftSecs !== undefined) {
               this.arenaTimer.sync(status.timeLeftSecs);
            }
            if (status.opponentHasSubmitted) {
               this.opponentFinished();
            }
            // Mark duel as active if IN_PROGRESS
            if (status.status === 'IN_PROGRESS') {
              this.isDuelActive.set(true);
            }
            // If already submitted, show the waiting panel
            if (status.hasSubmitted && !this.submissionResult()) {
               this.submissionResult.set({
                  verdict: 'submitted',
                  headline: 'Code Submitted',
                  summary: 'Waiting for opponent to finish...',
                  testCases: []
               });
               this.resultPanelOpen.set(true);
            }
            if (status.status === 'EVALUATING') {
              this.handleDuelEvent({ type: 'DUEL_EVALUATING' });
            } else if (status.status === 'COMPLETED' || status.status === 'DRAW') {
               this.handleDuelEvent({
                  type: 'DUEL_COMPLETED',
                  winnerId: status.winnerId,
                  challengerScore: status.challengerScore,
                  opponentScore: status.opponentScore,
                  challengerEloDelta: status.challengerEloDelta
               });
            }
          }
        });
      },
      error: (err) => {
        console.error('Failed to load active duel:', err);
        // Se não há duel ativo, o guard já deveria ter bloqueado.
        // Redirect de segurança.
        this.router.navigate(['/lobby']);
      }
    });
  }

  private handleDuelEvent(event: any): void {
    console.log('Duel WS Event:', event);
    switch (event.type) {
      case 'DUEL_TICK':
        if (this.arenaTimer) {
          this.arenaTimer.sync(event.timeLeftSecs);
        }
        break;
      case 'DUEL_TIME_REDUCED':
        if (this.arenaTimer) {
          this.arenaTimer.sync(event.timeLeftSecs);
        }
        break;
      case 'DUEL_STARTED':
        this.isDuelActive.set(true);
        break;
      case 'DUEL_TIMEOUT':
        this.isDuelActive.set(false);
        this.onTimeUp();
        break;
      case 'DUEL_EVALUATING':
        this.isDuelActive.set(false);
        // The server is judging submissions
        this.resultPanelOpen.set(true);
        this.submissionResult.set({
           verdict: 'evaluating',
           headline: 'Evaluating...',
           summary: 'Judging code',
           testCases: []
        });
        break;
      case 'DUEL_COMPLETED':
        this.isDuelActive.set(false);
        // Handle completed state
        this.resultPanelOpen.set(true);
        let headline = event.winnerId === 'DRAW' ? 'Draw!' : (event.winnerId === this.myId() ? 'You Won!' : 'You Lost!');
        if (event.reason === 'TIMEOUT' && event.winnerId !== 'DRAW') {
          headline = event.winnerId === this.myId() ? 'Won by Timeout!' : 'Lost by Timeout!';
        }
        this.submissionResult.set({
           verdict: 'success',
           headline: headline,
           summary: `Score: ${event.challengerScore} vs ${event.opponentScore}. Elo: ${event.challengerEloDelta > 0 ? '+' : ''}${event.challengerEloDelta}`,
           testCases: []
        });
        
        // Auto-redirect to lobby after 10 seconds
        setTimeout(() => this.router.navigate(['/lobby']), 10000);
        break;
      case 'DUEL_OPPONENT_FINISHED':
        if (event.username !== this.userService.username()) {
          this.opponentFinished();
        }
        break;
    }
  }

  // Need myId to determine win/loss
  myId(): number {
     return this.userService.currentUser()?.id ?? 0;
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
    this.subs.unsubscribe();
    this.wsService.disconnect();
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
    const did = this.duelId();
    if (!did || this.submissionResult()?.verdict === 'submitted' || this.submissionResult()?.verdict === 'evaluating') return;

    this.duelService.submitCode(did, { code: this.code(), language: this.selectedLanguage() }).subscribe({
      next: (res) => {
        console.log('Submission accepted by server:', res);
        this.submissionResult.set({
           verdict: 'submitted',
           headline: 'Code Submitted',
           summary: 'Waiting for opponent to finish...',
           testCases: []
        });
        this.resultPanelOpen.set(true);
        this.closeRunPanel();
      },
      error: (err) => {
        console.error('Failed to submit code:', err);
      }
    });
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
   * Chamado quando o utilizador clica o botão "Leave Arena" (pós-duel).
   * O botão só aparece quando o duel terminou (verdict === 'success').
   * Durante o duel, a navegação é bloqueada pelo arenaDeactivateGuard.
   */
  leaveArena(): void {
    this.router.navigate(['/lobby']);
  }
}
