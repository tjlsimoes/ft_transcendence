import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

// Navbar fixa do site com links de navegação e ações de autenticação.
@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {}
