import {
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild,
  input,
  output,
} from '@angular/core';
import loader from '@monaco-editor/loader';
import type * as Monaco from 'monaco-editor';

export type EditorTheme = 'Dark' | 'Light';
export type EditorLanguage = 'c' | 'cpp' | 'java' | 'python' | 'javascript';

const LANGUAGE_MAP: Record<string, string> = {
  C: 'c',
  'C++': 'cpp',
  Java: 'java',
  'Python 3': 'python',
  JavaScript: 'javascript',
};

/**
 * Thin Monaco Editor wrapper.
 * Renders a read-only editor pre-loaded with the given code/language/theme.
 * TODO: remove readOnly constraint once the full editor feature is implemented.
 */
@Component({
  selector: 'app-code-editor',
  template: `<div #editorContainer class="code-editor-container"></div>`,
  styles: [`
    :host { display: flex; flex: 1; overflow: hidden; }
    .code-editor-container { width: 100%; height: 100%; }
  `],
})
export class CodeEditorComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild('editorContainer', { static: true })
  private containerRef!: ElementRef<HTMLDivElement>;

  /** Source code to display. */
  @Input() value = '';

  /** Display language name (C, C++, Java, Python 3, JavaScript). */
  @Input() language = 'C';

  /** Visual theme. */
  @Input() theme: EditorTheme = 'Dark';

  /** Emits whenever the editor content changes (future use). */
  readonly valueChange = output<string>();

  private editor: Monaco.editor.IStandaloneCodeEditor | null = null;
  private monaco: typeof Monaco | null = null;

  ngOnInit(): void {
    loader.config({ paths: { vs: 'assets/monaco/vs' } });

    loader.init().then((monaco) => {
      this.monaco = monaco;
      this.defineCustomThemes(monaco);

      const editor = monaco.editor.create(this.containerRef.nativeElement, {
        value: this.value,
        language: LANGUAGE_MAP[this.language] ?? 'plaintext',
        theme: this.resolveTheme(),
        readOnly: true,           // ← read-only base; remove when implementing full editor
        automaticLayout: true,
        minimap: { enabled: false },
        scrollBeyondLastLine: false,
        fontFamily: '"JetBrains Mono", "Courier New", monospace',
        fontSize: 13,
        lineHeight: 20,
        renderLineHighlight: 'none',
        overviewRulerLanes: 0,
        hideCursorInOverviewRuler: true,
        scrollbar: {
          verticalScrollbarSize: 6,
          horizontalScrollbarSize: 6,
        },
        padding: { top: 12, bottom: 12 },
      });
      this.editor = editor;

      // Forward content changes for future use
      editor.onDidChangeModelContent(() => {
        this.valueChange.emit(editor.getValue());
      });
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.editor || !this.monaco) return;

    if (changes['value'] && !changes['value'].firstChange) {
      const current = this.editor.getValue();
      if (current !== this.value) {
        this.editor.setValue(this.value);
      }
    }

    if (changes['language'] && !changes['language'].firstChange) {
      const model = this.editor.getModel();
      if (model) {
        this.monaco.editor.setModelLanguage(model, LANGUAGE_MAP[this.language] ?? 'plaintext');
      }
    }

    if (changes['theme'] && !changes['theme'].firstChange) {
      this.monaco.editor.setTheme(this.resolveTheme());
    }
  }

  ngOnDestroy(): void {
    this.editor?.dispose();
  }

  // ── Helpers ────────────────────────────────────────────────────────────

  private resolveTheme(): string {
    return this.theme === 'Light' ? 'arena-light' : 'arena-dark';
  }

  private defineCustomThemes(monaco: typeof Monaco): void {
    monaco.editor.defineTheme('arena-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': '#0d0f11',
        'editor.foreground': '#e0e0e0',
        'editorLineNumber.foreground': '#404040',
        'editorLineNumber.activeForeground': '#606060',
        'editor.lineHighlightBackground': '#00000000',
        'editorCursor.foreground': '#00f33d',
        'editor.selectionBackground': '#00f33d33',
        'scrollbar.shadow': '#00000000',
        'scrollbarSlider.background': '#ffffff18',
        'scrollbarSlider.hoverBackground': '#ffffff28',
        'scrollbarSlider.activeBackground': '#ffffff38',
      },
    });

    monaco.editor.defineTheme('arena-light', {
      base: 'vs',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': '#f5f5f5',
        'editor.foreground': '#1a1a1a',
        'editorLineNumber.foreground': '#aaaaaa',
        'editorLineNumber.activeForeground': '#888888',
      },
    });
  }
}
