import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/components/navbar/navbar';
import { FloatingSymbols } from './shared/components/floating-symbols/floating-symbols';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, FloatingSymbols],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
