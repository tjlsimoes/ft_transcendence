import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit } from '@angular/core';

// Estrutura de cada símbolo desenhado no canvas de fundo.
interface FloatingSymbol {
  text: string;
  x: number;
  y: number;
  size: number;
  opacity: number;
  speedX: number;
  speedY: number;
}

// Camada visual decorativa com símbolos de C animados em canvas fixo.
@Component({
  selector: 'app-floating-symbols',
  imports: [],
  templateUrl: './floating-symbols.html',
  styleUrl: './floating-symbols.css',
})
export class FloatingSymbols implements AfterViewInit, OnDestroy {
  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  // Contexto 2D usado para desenhar o conteúdo no canvas.
  private ctx!: CanvasRenderingContext2D;
  // Coleção atual de símbolos animados.
  private symbols: FloatingSymbol[] = [];
  // ID do requestAnimationFrame para permitir cancelamento no destroy.
  private animationId = 0;

  // Vocabulário de tokens que aparecem no fundo.
  private readonly C_SYMBOLS = [
    '#include', 'int', 'char', 'void', 'return',
    'printf', 'malloc', 'free', 'sizeof', 'struct',
    'if', 'else', 'while', 'for', '#define',
    '{}', '()', '[]', '->', '**', '&', '*',
    'NULL', '0x', 'main()', 'argv', 'argc',
    'stdin', 'stdout', 'const', 'static', 'typedef',
  ];

  // Inicializa canvas, símbolos e loop de animação após render da view.
  ngAfterViewInit(): void {
    const canvas = this.canvasRef.nativeElement;
    this.ctx = canvas.getContext('2d')!;
    this.resize();
    this.initSymbols();
    this.animate();

    window.addEventListener('resize', this.resizeHandler);
  }

  // Limpa recursos de animação e listener ao destruir componente.
  ngOnDestroy(): void {
    cancelAnimationFrame(this.animationId);
    window.removeEventListener('resize', this.resizeHandler);
  }

  // Handler de resize reaproveitado para registrar/remover listener.
  private resizeHandler = () => this.resize();

  // Ajusta o tamanho do canvas para acompanhar viewport atual.
  private resize(): void {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }

  // Cria lote inicial de símbolos com quantidade proporcional à largura da tela.
  private initSymbols(): void {
    const count = Math.floor(window.innerWidth / 40);
    this.symbols = [];

    for (let i = 0; i < count; i++) {
      this.symbols.push(this.createSymbol());
    }
  }

  // Gera um símbolo com posição e velocidade aleatórias.
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

  // Loop principal: atualiza posição, recicla limites e desenha todos os símbolos.
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
