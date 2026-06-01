import { Component, effect, ElementRef, inject, input, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { ConversationWindow } from "../../models/chat.model";
import { ChatService } from "../../../core/services/chat.service";
import { ChatStateService } from "../../../core/services/chat-state.service";
import { UserService } from "../../../core/services/user.service";
import { Subscription } from "rxjs";
import { DatePipe } from "@angular/common";
import { FormsModule } from "@angular/forms";

@Component({
  selector: 'app-chat-window',
  imports: [FormsModule, DatePipe],
  templateUrl: './chat-window.html',
  styleUrl: './chat-window.css'
})
export class ChatWindow implements OnInit, OnDestroy {
  win = input.required<ConversationWindow>();

  private chatService = inject(ChatService);
  private chatState = inject(ChatStateService);
  private userService = inject(UserService);
  private subscription?: Subscription;
  @ViewChild('messageList') private messageList!: ElementRef<HTMLDivElement>

  messageText = '';

  constructor() {
    effect(() => {
      this.win().messages;  // reading this signal, registers the dependency
      setTimeout(() => this.scrollToBottom(), 0); // ensure effect is applied to new DOM
    });
  }

  ngOnInit(): void {
    const friendId = this.win().friend.id;
    const myId = this.userService.currentUser()!.id;

    this.chatService.getHistory(friendId).subscribe(page => {
      this.chatState.prependHistory(friendId, page.content);
    })
    
    this.subscription = this.chatService
      .subscribeToConversation(myId, friendId)
      .subscribe(msg => {
        this.chatState.addMessage(friendId, msg);
        if (this.win().minimized) {
          this.chatState.incrementUnread(friendId);
        }
      })
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  send(): void {
    const text = this.messageText.trim();
    if (!text) return;
    this.chatService.sendMessage(this.win().friend.id, text);
    this.messageText = '';
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  toggleMinimize(): void {
    const wasMinimized = this.win().minimized;
    this.chatState.toggleMinimize(this.win().friend.id);
    if (wasMinimized) {
      this.chatState.markRead(this.win().friend.id);
    }
  }

  close(): void {
    this.chatState.closeConversation(this.win().friend.id);
  }

  isMine(senderId: number): boolean {
    return senderId === this.userService.currentUser()?.id;
  }

  getAvatarLetter(username: string): string {
    return username ? username.charAt(0).toUpperCase() : '?';
  }

  // How far the element is scrolled from the top
  private scrollToBottom(): void {
    const el = this.messageList?.nativeElement;
    if (el) el.scrollTop = el.scrollHeight;
  }
}