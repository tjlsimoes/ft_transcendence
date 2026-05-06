// ── Submission result types ────────────────────────────────────────────────

export type TestCaseStatus = 'passed' | 'failed';

export interface TestCaseResult {
  label: string;           // e.g. "Sample Test case 0"
  status: TestCaseStatus;
  compilerMessage?: string;
  stdin?: string;
  stdout?: string;         // your output
  expectedOutput?: string;
}

export type SubmissionVerdict = 'correct' | 'wrong' | 'runtime_error' | 'time_limit';

export interface SubmissionResult {
  verdict: SubmissionVerdict;
  /** Human-readable headline, e.g. "Wrong Answer :(" */
  headline: string;
  /** e.g. "2/2 test cases failed" */
  summary: string;
  testCases: TestCaseResult[];
}

// ── Run result types ───────────────────────────────────────────────────────

export type RunStatus = 'success' | 'compile_error' | 'runtime_error' | 'timeout';

export interface RunResult {
  status: RunStatus;
  /** e.g. "Compiled & Executed" or "Compilation Error" */
  headline: string;
  compilerMessage?: string;
  stdin?: string;
  stdout?: string;
  stderr?: string;
  /** Execution time in ms */
  executionTimeMs?: number;
}

// ── Mock helpers ───────────────────────────────────────────────────────────
/**
 * Simulates a WRONG answer response from the backend.
 * Replace with a real HTTP response mapping when the backend is ready.
 */
export function mockWrongAnswerResult(): SubmissionResult {
  return {
    verdict: 'wrong',
    headline: 'Wrong Answer :(',
    summary: '2/2 test cases failed',
    testCases: [
      {
        label: 'Sample Test case 0',
        status: 'failed',
        compilerMessage: 'Wrong Answer',
        stdin: '6\n16 13 7 2 1 12',
        stdout: '01',
        expectedOutput: '51',
      },
      {
        label: 'Sample Test case 1',
        status: 'failed',
        compilerMessage: 'Wrong Answer',
        stdin: '4\n1 2 3 4',
        stdout: '0',
        expectedOutput: '10',
      },
    ],
  };
}

/**
 * Simulates a CORRECT answer response from the backend.
 */
export function mockCorrectAnswerResult(): SubmissionResult {
  return {
    verdict: 'correct',
    headline: 'Correct Answer! :)',
    summary: '2/2 test cases passed',
    testCases: [
      { label: 'Sample Test case 0', status: 'passed' },
      { label: 'Sample Test case 1', status: 'passed' },
    ],
  };
}

/**
 * Simulates a successful run response from the backend.
 * Replace with a real HTTP response mapping when the backend is ready.
 */
export function mockRunSuccess(): RunResult {
  return {
    status: 'success',
    headline: 'Compiled & Executed',
    stdin: '6\n16 13 7 2 1 12',
    stdout: '01',
    executionTimeMs: 42,
  };
}

/**
 * Simulates a compile error run response from the backend.
 */
export function mockRunCompileError(): RunResult {
  return {
    status: 'compile_error',
    headline: 'Compilation Error',
    compilerMessage: "solution.c:7:5: error: use of undeclared identifier 'prtinf'\n  prtinf(\"%d\\n\", sum);\n  ^",
    stdin: '6\n16 13 7 2 1 12',
  };
}
