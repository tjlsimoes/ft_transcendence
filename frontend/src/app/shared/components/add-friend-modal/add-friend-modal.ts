import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { UserService } from '../../../core/services/user.service';
import { UserSearchResult } from '../../models/user-profile.model';
import { Modal } from '../modal/modal';

@Component({
  selector: 'app-add-friend-modal',
  imports: [Modal],
  templateUrl: './add-friend-modal.html',
  styleUrl: './add-friend-modal.css',
})
export class AddFriendModal implements OnInit {
  open = input.required<boolean>();
  closed = output<void>();

  private userService = inject(UserService);
  private searchTerm$ = new Subject<string>();

  results = signal<UserSearchResult[]>([]);
  query = '';

  ngOnInit(): void {
    this.searchTerm$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => q.trim().length >= 2 ? this.userService.searchUsers(q.trim()) : of([]))
    ).subscribe(results => this.results.set(results));
  }

  onInput(value: string): void {
    this.query = value;
    this.searchTerm$.next(value);
  }

  sendRequest(userId: number): void {
    this.userService.sendFriendRequest(userId).subscribe(() => {
      this.results.update(list => list.map(r =>
        r.id === userId ? { ...r, relationshipStatus: 'PENDING_OUTGOING' as const } : r
      ));
    });
  }

  close(): void {
    this.query = '';
    this.results.set([]);
    this.closed.emit();
  }
}
