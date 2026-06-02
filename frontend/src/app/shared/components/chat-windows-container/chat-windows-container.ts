import { Component, inject } from "@angular/core";
import { ChatStateService } from "../../../core/services/chat-state.service";
import { ChatWindow } from "../chat-window/chat-window";

@Component({
  selector: 'app-chat-windows-container',
  imports: [ChatWindow],          // needed so the template can use <app-chat-window>
  templateUrl: './chat-windows-container.html',
  styleUrl: './chat-windows-container.css',
})
export class ChatWindowsContainer {
  chatState = inject(ChatStateService);
}
