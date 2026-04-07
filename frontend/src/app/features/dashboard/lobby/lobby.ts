import { Component, signal } from '@angular/core';
import { PlayerStats } from './components/player-stats/player-stats';
import { ProfileData } from './components/player-stats/profile-data/profile-data';
import { TerminalHistory, TerminalMatchHistory } from './components/player-stats/terminal-history/terminal-history';

interface LobbyTab {
  id: string;
  label: string;
  active?: boolean;
}

interface LobbyEvent {
  id: string;
  type: 'system' | 'queue' | 'match';
  text: string;
}

@Component({
  selector: 'app-lobby',
  imports: [PlayerStats, ProfileData, TerminalHistory],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby {
  isQueueing = signal(false);
  queueTime = signal('00:00');
  queueInterval: any;

  // Mock data for Detailed Stats
  currentLeague = 'GOLD II';
  currentLp = 2350;
  nextRankLp = 2400;
  winRate = 64.2;

  get lpProgress() {
    return (this.currentLp / this.nextRankLp) * 100;
  }

  matchHistory: TerminalMatchHistory[] = [
    { result: 'VICTORY', lpChange: 24, opponent: 'xSniper99', date: '2 mins ago' },
    { result: 'DEFEAT', lpChange: -15, opponent: 'leet_coder', date: '1 hr ago' },
    { result: 'VICTORY', lpChange: 20, opponent: 'algo_master', date: 'Yesterday' },
    { result: 'DEFEAT', lpChange: -10, opponent: 'pro_gamer', date: '2 days ago' },
    { result: 'VICTORY', lpChange: 18, opponent: 'championX', date: '3 days ago' },
  ];

  tabs: LobbyTab[] = [
    { id: 'lobby', label: 'lobby.html', active: true },
    { id: 'styles', label: 'lobby.css' },
  ];

  toggleQueue() {
    this.isQueueing.set(!this.isQueueing());
    if (this.isQueueing()) {
      let seconds = 0;
      this.queueInterval = setInterval(() => {
        seconds++;
        const mins = Math.floor(seconds / 60).toString().padStart(2, '0');
        const secs = (seconds % 60).toString().padStart(2, '0');
        this.queueTime.set(`${mins}:${secs}`);
      }, 1000);
    } else {
      clearInterval(this.queueInterval);
      this.queueTime.set('00:00');
    }
  }

  quickCommands: string[] = [
    '/queue ranked',
    '/queue classic',
    '/profile open',
    '/history latest',
  ];
}
