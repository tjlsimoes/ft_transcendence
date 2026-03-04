import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

// Seção principal da landing page com chamada para cadastro.
@Component({
  selector: 'app-hero',
  imports: [RouterLink],
  templateUrl: './hero.html',
  styleUrl: './hero.css',
})
export class Hero {}
