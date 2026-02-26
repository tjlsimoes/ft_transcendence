import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit } from '@angular/core';

interface FloatingSymbol {
  text: string;
  x: number;
  y: number;
  size: number;
  opacity: number;
  speedX: number;
  speedY: number;
}

@Component({
  selector: 'app-floating-symbols',
  imports: [],
  templateUrl: './floating-symbols.html',
  styleUrl: './floating-symbols.css',
})
export class FloatingSymbols implements AfterViewInit, OnDestroy {
  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  private ctx!: CanvasRenderingContext2D;
  private symbols: FloatingSymbol[] = [];
  private animationId = 0;

  private readonly C_SYMBOLS = [
    '#include', 'int', 'char', 'void', 'return',
    'printf', 'malloc', 'free', 'sizeof', 'struct',
    'if', 'else', 'while', 'for', '#define',
    '{}', '()', '[]', '->', '**', '&', '*',
    'NULL', '0x', 'main()', 'argv', 'argc',
    'stdin', 'stdout', 'const', 'static', 'typedef',
  ];

  ngAfterViewInit(): void {
    const canvas = this.canvasRef.nativeElement;
    this.ctx = canvas.getContext('2d')!;
    this.resize();
    this.initSymbols();
    this.animate();

    window.addEventListener('resize', this.resizeHandler);
  }

  ngOnDestroy(): void {
    cancelAnimationFrame(this.animationId);
    window.removeEventListener('resize', this.resizeHandler);
  }

  private resizeHandler = () => this.resize();

  private resize(): void {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }

  private initSymbols(): void {
    const count = Math.floor(window.innerWidth / 40);
    this.symbols = [];

    for (let i = 0; i < count; i++) {
      this.symbols.push(this.createSymbol());
    }
  }

  private createSymbol(): FloatingSymbol {
    return {
      text: this.C_SYMBOLS[Math.floor(Math.random() * this.C_SYMBOLS.length)],
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      size: 10 + Math.random() * 14,
      opacity: 0.12 + Math.random() * 0.08,
      speedX: (Math.random() - 0.5) * 0.3,
      speedY: (Math.random() - 0.5) * 0.2,
    };
  }

  private animate = (): void => {
    const canvas = this.canvasRef.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (const s of this.symbols) {
      s.x += s.speedX;
      s.y += s.speedY;

      if (s.x < -100) s.x = canvas.width + 50;
      if (s.x > canvas.width + 100) s.x = -50;
      if (s.y < -50) s.y = canvas.height + 30;
      if (s.y > canvas.height + 50) s.y = -30;

      this.ctx.font = `${s.size}px 'Courier New', monospace`;
      this.ctx.fillStyle = `rgba(0, 255, 65, ${s.opacity})`;
      this.ctx.fillText(s.text, s.x, s.y);
    }

    this.animationId = requestAnimationFrame(this.animate);
  };
}
