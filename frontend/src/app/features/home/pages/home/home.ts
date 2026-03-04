import { Component } from '@angular/core';
import { Hero } from '../../components/hero/hero';

// Página Home: atualmente renderiza apenas a seção Hero principal.
@Component({
  selector: 'app-home',
  imports: [Hero],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {}
