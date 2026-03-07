# Git & GitHub Policy — Code Arena
> ft_transcendence · Version 1.0

---

## Table of Contents

1. [Branch Naming](#1-branch-naming)
2. [Commit Messages](#2-commit-messages)
3. [Merging Strategy](#3-merging-strategy)
4. [Branch Protection Rules](#4-branch-protection-rules)
5. [Pull Request Workflow](#5-pull-request-workflow)
6. [Code Review Standards](#6-code-review-standards)
7. [Issue & Project Board Usage](#7-issue--project-board-usage)
8. [Communication Rules](#8-communication-rules)
9. [Security Rules](#9-security-rules)
10. [ft_transcendence Compliance](#10-fttranscendence-compliance)
11. [Quick Reference Card](#11-quick-reference-card)

---

## 1. Branch Naming

### Protected branches

| Branch | Description |
|---|---|
| `main` | Production-ready code. Protected — no direct commits, ever. |

### Working branches

| Pattern | Use case | Example |
|---|---|---|
| `feature/<description>` | New feature or user-facing work | `feature/monaco-editor` |
| `feat/<description>` | Alias for feature | `feat/elo-ranking` |
| `fix/<description>` | Bug fix | `fix/jwt-expiry-check` |
| `hotfix/<description>` | Urgent fix against `main` | `hotfix/sandbox-timeout` |
| `chore/<description>` | Non-feature work (deps, config, CI) | `chore/docker-healthcheck` |
| `docs/<description>` | Documentation only | `docs/readme-db-schema` |
| `refactor/<description>` | Code restructure, no behaviour change | `refactor/extract-judge-service` |
| `release/v<x.y>` | Final release — end of project only | `release/v1.0` |

### Rules
- Lowercase with hyphens only — no spaces, underscores, or CamelCase
- Descriptions are short (2–4 words) and self-explanatory at a glance
- Always branch off `main`
- Delete branches after the PR is merged — keep the remote clean

---

## 2. Commit Messages

We follow the **[Conventional Commits](https://www.conventionalcommits.org/)** specification.

### Format

```
<type>(<scope>): <short description>

[optional body — explain the why, not the what]

[optional footer — e.g. Closes #42, Co-authored-by: ...]
```

### Types

| Type | When to use |
|---|---|
| `feat` | A new feature or user-facing behaviour |
| `fix` | A bug fix |
| `docs` | Documentation changes only |
| `style` | Formatting, whitespace — no logic change |
| `refactor` | Code restructure without behaviour change |
| `test` | Adding or updating tests |
| `chore` | Build process, deps, CI, config — no production code |
| `perf` | Performance improvement |

### Scopes

Use the most specific scope that applies:

`auth` · `arena` · `judge` · `matchmaking` · `ranking` · `chat` · `notifications` · `user` · `challenges` · `docker` · `db` · `nginx` · `websocket` · `frontend` · `readme`

### Short description rules
- **Imperative mood** — "add endpoint" not "added endpoint"
- **No capital letter** at the start
- **No full stop** at the end
- **50 characters maximum** — use the body if you need more

### Examples

```bash
# ✅ Good
feat(arena): integrate Monaco editor with Python syntax highlighting
fix(judge): kill container on hard timeout instead of blocking
docs(readme): update module points table with ORM justification
chore(docker): add Redis healthcheck to compose
refactor(matchmaking): extract Elo pairing logic into EloService
test(auth): add brute-force rate-limit integration test
perf(db): add index on submissions.duel_id for leaderboard query

# ❌ Bad
Added the login feature          # past tense, no type or scope
WIP                              # meaningless
fix stuff                        # too vague
feat(auth): Added JWT tokens.    # capital letter, full stop, past tense
```

### When to use the body

Use the body when the *why* is not obvious from the description alone:

```
refactor(judge): replace polling with Docker events API

Polling every 500ms caused unnecessary CPU load under concurrent duels.
Switching to the Docker events stream reduces resource usage and
simplifies the timeout logic.
```

### Footer — issue references and co-authors

```
feat(chat): add unread message badge to navbar

Closes #47
Co-authored-by: Alice <alice@42.fr>
```

Use `Co-authored-by` when pair-programming — both contributors then appear in the
GitHub contribution graph, which matters for the evaluation's git history check.

### Commit granularity

> One commit = one logical, self-contained change.

Commit often inside your feature branch — small commits are easy to review and revert.
Avoid mixing unrelated changes in a single commit. Branch commits get squashed on
merge to `main` anyway, so tidiness there matters less than the final PR title.

---

## 3. Merging Strategy

### Default: Squash and Merge

All PRs are merged to `main` using **Squash and Merge** only.

```
feature/monaco-editor            main (after merge)
─────────────────────            ──────────────────────────────
commit: WIP                  →   feat(arena): integrate Monaco editor
commit: trying approach B        with Python syntax highlighting
commit: fix linting
commit: final cleanup            (one clean commit on main)
```

**Why squash?**
- `main` stays linear and readable
- Each merged PR = one meaningful commit on `main`
- WIP, typo-fix, and "oops" commits disappear from the permanent history
- Easy to revert an entire feature: `git revert <squash-commit-hash>`
- Evaluators see a clean, professional history

### When to use alternatives (rarely)

| Strategy | When | Who decides |
|---|---|---|
| Merge commit | Preserving detailed branch history is explicitly valuable | TL + SM agreement |
| Rebase and merge | **Avoid** — high conflict risk with concurrent part-time work | Not recommended |

### Enforce in repository settings
- ✅ Allow squash merging only
- ❌ Disable merge commits
- ❌ Disable rebase and merge

---

## 4. Branch Protection Rules

Configure in **Settings → Branches → Branch protection rules → Add rule** for `main`.

| Setting | Value |
|---|---|
| Require a pull request before merging | ✅ Enabled |
| Required approvals | 1 minimum |
| Dismiss stale reviews on new commits | ✅ Enabled |
| Require status checks to pass | ✅ Enabled (if CI configured) |
| Require branches to be up to date | ✅ Enabled |
| Allow force pushes | ❌ Disabled |
| Allow deletions of `main` | ❌ Disabled |

### Who must review what

| PR touches | Required reviewer |
|---|---|
| DB schema / migrations | Tech Lead (mandatory) |
| Auth / JWT / security logic | Tech Lead (mandatory) |
| Judge sandbox / Docker runner | Tech Lead (mandatory) |
| WebSocket / real-time infrastructure | Tech Lead or backend developer |
| Frontend UI only | Any peer |
| Documentation / README only | Any peer (SM can self-merge if trivial) |

---

## 5. Pull Request Workflow

### Step by step

```bash
# 1. Always start from an up-to-date main
git checkout main && git pull

# 2. Create your branch
git checkout -b feature/my-feature

# 3. Work and commit often
git add -p                          # stage by hunk, not blindly
git commit -m "feat(scope): ..."

# 4. Stay up to date with main regularly
git fetch origin
git rebase origin/main

# 5. Push and open PR
git push -u origin feature/my-feature
gh pr create --fill                 # or open in browser
```

### PR title

The PR title becomes the squash commit message on `main`. It must follow Conventional Commits:

```
feat(arena): add Monaco editor with Python syntax highlighting
fix(judge): handle container OOM kill gracefully
chore(docker): add Redis healthcheck to compose
```

### PR description template

Save this as `.github/pull_request_template.md` and GitHub pre-fills it on every new PR:

```markdown
## What
[What does this PR do? One paragraph.]

## Why
[Why was this change needed?]

## How
[Non-obvious implementation decisions. Skip if self-evident.]

## Screenshots / recordings
[Required for all frontend changes. Before/after for visual fixes.]

## Checklist
- [ ] All acceptance criteria from the linked issue are met
- [ ] `docker compose up --build` still works
- [ ] No console errors in Chrome DevTools
- [ ] Tests added or updated where applicable
- [ ] README updated if setup, tech stack, or modules are affected

Closes #[issue number]
```

### PR size guidelines

| Size | Lines changed | Approach |
|---|---|---|
| Small | < 200 | Ideal — reviewable in 15–20 min |
| Medium | 200–500 | Acceptable — add extra context in description |
| Large | 500–1000 | Split if possible; offer a walkthrough if not |
| Too large | > 1000 | Almost always a scoping problem |

On a part-time team, large PRs are particularly costly — a reviewer with limited hours
will naturally delay a 1,000-line diff. Smaller PRs get reviewed faster and unblock
people sooner.

### Keeping PRs moving

- If your PR has been open for **48h without review**, ping the reviewer in Discord with the link
- If there are merge conflicts, **the PR author** resolves them by rebasing onto main
- Do not force-push to a branch with an open PR without warning the reviewer first

---

## 6. Code Review Standards

### Reviewer responsibilities

- **Respond within 24h** on working days — "I'll look properly tomorrow" beats silence
- Review against the **acceptance criteria in the linked issue**, not personal style preferences
- Test the feature locally for non-trivial backend, security, or real-time changes

### Comment prefixes

| Prefix | Meaning | Must be resolved before merge? |
|---|---|---|
| `blocker:` | Functional issue, security risk, or acceptance criteria not met | ✅ Yes |
| `suggestion:` | Better approach available but current code works | Reviewer's call |
| `nit:` | Minor style or naming preference | ❌ Author's discretion |
| `question:` | Genuine curiosity — no change required | ❌ No |
| `praise:` | Something done particularly well | ❌ No |

### What to check — all PRs
- Does the code meet the acceptance criteria?
- Are there obvious edge cases not handled?
- Are error states handled gracefully?
- Is there duplicated logic that should be extracted?

### What to check — backend PRs
- Are all inputs validated (Jakarta Bean Validation or equivalent)?
- Is DB access going through JPA repositories — no raw SQL outside of migrations?
- Are sensitive values never returned in responses or logged?
- Does every protected endpoint have the correct security annotation?

### What to check — frontend PRs
- Does the component work at 375px (mobile) and 1280px (desktop)?
- Are loading and error states present for async operations?
- Is the JWT in memory — not in `localStorage`?
- Does it run in Chrome with zero console errors?

### What to check — Docker / infra PRs
- Does `docker compose up --build` succeed from scratch?
- Are new environment variables added to `.env.example`?
- Are there any hardcoded secrets in Dockerfiles or compose files?

### Approval etiquette
- **Approve with comments** when comments are non-blocking nits or suggestions
- **Request changes** only for blockers — not for style preferences
- Once a blocker is addressed, re-review within 4h if possible
- Do not block a PR on preferences not covered by an agreed lint config

---

## 7. Issue & Project Board Usage

### Every piece of work needs an issue

No mystery work. If you are going to spend more than an hour on something, there must
be a GitHub Issue for it. The issue + PR trail is exactly what evaluators examine when
checking git history and team contributions.

### Issue structure

**Title**: Conventional Commits format — `feat(scope): short description`

**Body**:
```markdown
## Goal
[Why does this issue exist? What problem does it solve?]

## Acceptance Criteria
- [ ] Specific, testable condition 1
- [ ] Specific, testable condition 2

## Notes
[Implementation hints, links, known gotchas]
```

### Labels

| Category | Labels |
|---|---|
| Mandatory vs bonus | `mandatory`, `bonus` |
| Module type | `module:major`, `module:minor` |
| Area | `area:frontend`, `area:backend`, `area:devops`, `area:security`, `area:db`, `area:realtime`, `area:auth`, `area:judge` |
| Sprint | `sprint:1`, `sprint:2`, `sprint:3`, `sprint:4` |
| Priority | `priority:critical`, `priority:high`, `priority:medium`, `priority:low` |
| Special | `status:blocked` |

### Board columns

| Column | Meaning |
|---|---|
| **Backlog** | Groomed issues not yet assigned to a sprint |
| **To Do** | Committed to the current sprint, not started |
| **In Progress** | Actively being worked on (one owner) |
| **In Review** | PR is open and awaiting review |
| **Done** | PR merged, Definition of Done fully met |

### Board hygiene (PM enforces)
- Move to "In Progress" only when work actually starts — not at sprint planning
- Move to "In Review" when the PR is opened — not when it is merged
- Move to "Done" only after the PR is merged to `main`
- No issue stays "In Progress" for more than 5 days without a standup mention
- PM audits and updates the board before every Tuesday meeting

### Linking PRs to issues

Always include `Closes #<number>` in the PR description. GitHub closes the issue
automatically when the PR merges.

---

## 8. Communication Rules

| Situation | Channel |
|---|---|
| Daily standup | `#standup` on Discord (async, every working day) |
| Quick questions | `#general` on Discord |
| Blockers | `#blockers` on Discord + @mention in the GitHub issue |
| Architectural decisions | GitHub Discussions → **Decisions** category |
| PR review requests | GitHub notification + link in `#general` |
| Sprint planning / review / retro | Tuesday online meeting |

### Decision records

Any decision affecting the whole codebase or a module's architecture goes into a
GitHub Discussion under **Decisions**. Format:

```markdown
## Context
[What situation prompted this decision?]

## Decision
[What did we decide?]

## Alternatives considered
[What else did we consider, and why did we not choose it?]

## Consequences
[What does this mean for the codebase going forward?]
```

At evaluation, every team member should be able to explain key technical choices.
A written record means this does not depend on one person's memory.

---

## 9. Security Rules

These are non-negotiable and enforced at code review.

### Repository secrets
- **Never** commit `.env` files — `.gitignore` must include `.env` from day one
- **Always** provide `.env.example` with placeholder values for every variable
- Before any PR merges, verify the diff contains no real credentials
- If a secret is accidentally committed: **rotate it immediately**, then remove it
  from history with `git filter-repo`

### Code secrets
- No hardcoded passwords, tokens, API keys, or credentials anywhere in the codebase
- No secrets in Dockerfiles or `docker-compose.yml` — use environment variable injection only
- Database credentials, JWT secrets, and third-party keys load exclusively from
  environment variables

### Pre-push mental checklist

```
□ Does my diff contain any passwords, tokens, or API keys?
□ Did I accidentally stage a .env file?
□ Am I pushing directly to main? (Answer must be NO — always use PRs)
```

---

## 10. ft_transcendence Compliance

The evaluation sheet explicitly checks these git-related criteria:

| Evaluation criterion | How this policy addresses it |
|---|---|
| Commits from all team members visible on `main` | Each person owns their issues; squash merge puts their name on every merged feature commit |
| Commit messages are clear and meaningful | Conventional Commits required; checked at code review |
| Work is distributed across team members | Sprint planning assigns issues individually; PM audits via `git shortlog` after each sprint |
| No secrets in the repository | `.env` in `.gitignore` from day one; pre-merge checklist; dedicated audit in Sprint 4 |

### The `git shortlog` check

Run this after every sprint:

```bash
git shortlog -sn --no-merges
```

All four team members should appear with a reasonable number of commits. If someone
shows zero at the end of a sprint, address it before the next one — not on evaluation day.

### Pair programming and co-author credits

When two people work together (recommended for the Judge sandbox and WebSocket setup),
use the co-author trailer so both contributors appear in GitHub's contribution graph:

```bash
git commit -m "feat(judge): implement Docker sandbox runner

Co-authored-by: Alice <alice@42.fr>"
```

---

## 11. Quick Reference Card

```
┌─────────────────────────────────────────────────────────────────┐
│  DAILY WORKFLOW                                                 │
│                                                                 │
│  git checkout main && git pull        # always start fresh     │
│  git checkout -b feature/my-thing     # new branch             │
│  git add -p && git commit -m "..."    # commit often           │
│  git fetch && git rebase origin/main  # stay up to date        │
│  git push -u origin feature/my-thing  # push and open PR       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  COMMIT FORMAT                                                  │
│                                                                 │
│  feat(scope): what you added                                    │
│  fix(scope): what you fixed                                     │
│  chore(scope): infra / deps / config                           │
│  docs(scope): documentation only                               │
│  refactor(scope): restructure, same behaviour                  │
│  test(scope): tests only                                        │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  NEVER DO THESE                                                 │
│                                                                 │
│  ✗  git push origin main               # always use PRs        │
│  ✗  git commit -m "WIP"                # meaningless           │
│  ✗  git commit -m "fix stuff"          # too vague             │
│  ✗  staging .env files                 # security risk         │
│  ✗  force-pushing a branch with open PR                        │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  PR CHECKLIST (before requesting review)                       │
│                                                                 │
│  □ Title follows Conventional Commits                          │
│  □ Description: What / Why / Closes #XX                        │
│  □ All acceptance criteria met                                  │
│  □ docker compose up --build still works                       │
│  □ No console errors in Chrome                                  │
│  □ No .env or secrets in the diff                              │
│  □ Screenshot attached (frontend changes)                      │
└─────────────────────────────────────────────────────────────────┘
```

---

*This policy is a living document. Propose changes via PR to this file; requires TL + SM approval.*