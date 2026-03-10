# Project Roadmap — Code Arena
> ft_transcendence · 4 sprints · Mar 4 – Apr 29 · ~8–10h/week per role · Target: 16 module points

---

## Table of Contents

1. [At a Glance](#1-at-a-glance)
2. [Capacity Model](#2-capacity-model)
3. [Module Point Plan](#3-module-point-plan)
4. [Team & Work Division](#4-team--work-division)
5. [Sprint 1 — Foundation](#5-sprint-1--foundation-mar-4--mar-18)
6. [Sprint 2 — Core Backend & Auth](#6-sprint-2--core-backend--auth-mar-18--apr-1)
7. [Sprint 3 — Game Engine & Real-time](#7-sprint-3--game-engine--real-time-apr-1--apr-15)
8. [Sprint 4 — Frontend, Modules & Evaluation Prep](#8-sprint-4--frontend-modules--evaluation-prep-apr-15--apr-29)
9. [Risk Register](#9-risk-register)
10. [Evaluation Readiness Checklist](#10-evaluation-readiness-checklist)

---

## 1. At a Glance

```
Mar  4 ──┬── Sprint 1 ──┬── Foundation
Mar 18   │  (Tue–Tue)   │   Docker · DB schema · scaffolds · README
         │              │
Mar 18 ──┼── Sprint 2 ──┼── Core Backend & Auth
Apr  1   │  (Tue–Tue)   │   JWT auth · User profiles · Challenge bank · UI forms
         │              │
Apr  1 ──┼── Sprint 3 ──┼── Game Engine & Real-time  ⚠ hardest sprint
Apr 15   │  (Tue–Tue)   │   Judge sandbox · WebSockets · Matchmaking · Duel engine · Elo
         │              │
Apr 15 ──┼── Sprint 4 ──┼── Frontend, Modules & Evaluation Prep
Apr 29   │  (Tue–Tue)   │   Arena UI · Chat · Notifications · Gamification · Security · Hardening
         │
         └── Evaluation (soft target: end of April)
```

### Sprint calendar

| Sprint | Start | Review (Tue) | Goal |
|---|---|---|---|
| Sprint 1 | Tue Mar 4 | Tue Mar 18 | Foundation — single `docker compose up` works |
| Sprint 2 | Tue Mar 18 | Tue Apr 1 | Auth & Core Backend — register, log in, see profile |
| Sprint 3 | Tue Apr 1 | Tue Apr 15 | Game Engine & Real-time — two tabs can duel ⚠ |
| Sprint 4 | Tue Apr 15 | Tue Apr 29 | Frontend, all modules done, evaluation-ready |

---

## 2. Capacity Model

Understanding actual capacity is the difference between a realistic roadmap and an aspirational one.

### Per-person, per-sprint

| Availability | Hours/week | Hours per sprint (2 weeks) | Minus meetings & reviews | Effective coding hours |
|---|---|---|---|---|
| Conservative | 8h | 16h | ~2h | **~14h** |
| Moderate | 10h | 20h | ~2h | **~18h** |

### Team capacity per sprint

```
4 people × ~16h (conservative) = ~64h team capacity per sprint
4 sprints × ~64h               = ~256h total project capacity
```

### Issue sizing guide

| Issue type | Estimated hours |
|---|---|
| Simple backend endpoint (CRUD) | 2–4h |
| Full feature: endpoint + frontend component + tests | 6–10h |
| Complex feature: Judge sandbox, WebSocket infra | 10–15h |
| Infrastructure / Docker / config | 3–6h |
| Documentation / README section | 1–3h |

Expect **6–8 completed issues per sprint** across the team. Planning is based on this number.

### The 4-sprint constraint

With only 4 sprints, there is no sprint to "catch up" in. Every sprint that ends with significant unfinished work compresses the next one. Scope discipline matters more here than in a longer project.

**Rules:**
- Approximately 20% of capacity remains unplanned — roughly 1–2 issues of headroom per sprint.
- Flag reduced availability at planning (exams, deadlines, personal commitments)
- Bonus modules are only picked up after all 16 mandatory points are confirmed done
- "Mostly done" is not done — it goes back to the backlog

---

## 3. Module Point Plan

Target: **16 mandatory points** (2-point buffer above the required 14) + up to **2 bonus points**.

### Mandatory modules (16 pts)

| Module | Category | Type | Pts | Target Sprint |
|---|---|---|---|---|
| Frontend + Backend frameworks (Angular + Spring Boot) | Web | Major | 2 | S1 |
| Standard user management & authentication | User Mgmt | Major | 2 | S2 |
| Real-time features via WebSockets | Web | Major | 2 | S3 |
| Complete web-based game (Code Duel) | Gaming | Major | 2 | S3 |
| Remote players (two computers, real-time) | Gaming | Major | 2 | S3 |
| Game statistics & match history | User Mgmt | Minor | 1 | S3 |
| User interaction (chat + profile + friends) | Web | Major | 2 | S4 |
| ORM — Hibernate / Spring Data JPA | Web | Minor | 1 | S4 |
| Notification system | Web | Minor | 1 | S4 |
| Gamification (achievements, badges, XP) | Gaming | Minor | 1 | S4 |
| **Total** | | | **16** | |

### Bonus modules (only after mandatory is fully done)

| Module | Category | Type | Pts | Target Sprint |
|---|---|---|---|---|
| OAuth 2.0 (GitHub / 42) | User Mgmt | Minor | +1 | S4 |
| Spectator mode | Gaming | Minor | +1 | S4 |

> Bonus issues are **never** planned into a sprint alongside incomplete mandatory work.
> They are picked up only if all 16 mandatory points are accepted in sprint review.

### Module point progress tracker

Update this after every Tuesday Week 2 review:

| Sprint | Review date | Modules accepted | Points this sprint | Cumulative |
|---|---|---|---|---|
| Sprint 1 | Mar 18 | Frameworks (Angular + Spring Boot) | 2 | 2 |
| Sprint 2 | Apr 1 | Standard User Management | 2 | 4 |
| Sprint 3 | Apr 15 | WebSockets · Game · Remote players · Elo | 7 | 11 |
| Sprint 4 | Apr 29 | User Interaction · ORM · Notifications · Gamification | 5 | **16** |

> ⚠ If cumulative total is below **11 points after the Sprint 3 review (Apr 15)**,
> run an emergency scope session with the PO before Sprint 4 planning begins.

---

## 4. Team & Work Division

### Roles

| Person | Scrum Role | Project Role | Primary area |
|---|---|---|---|
| A | Product Owner | PO + Engineering | Frontend |
| B | Scrum Master / PM | PM + Engineering | Backend |
| C | Tech Lead | Tech Lead / Architecture + Engineering | Backend |
| D | Engineering | Engineering | Frontend |

### Division principles

- The frontend/backend split is **provisional and permeable**
- Features spanning both layers (chat, notifications, gamification) are owned by whoever builds the backend, but both pairs agree on the API contract **before** either side starts building
- The TL reviews all PRs touching the DB schema, auth, the Judge sandbox, or WebSocket infrastructure
- By Sprint 4, every team member should understand at least one feature they did not primarily build — evaluators check this knowledge.

### API contracts

For any cross-cutting feature, the PM facilitates a contract agreement at sprint planning: request shape, response shape, WebSocket message format. Recorded as a comment on the relevant GitHub issue. Frontend and backend must not build against each other's assumptions without this conversation.

---

## 5. Sprint 1 — Foundation (Mar 4 → Mar 18)

**Goal**: `docker compose up --build` starts the entire application. Frontend shows a stub page at `https://localhost`. Backend health endpoint responds. Database tables exist.

**Tuesday Mar 18 exit criteria**:
- `https://localhost` reachable (stub Angular page, no errors)
- `GET /api/health → 200`
- PostgreSQL and Redis containers healthy
- `db/migrations/V1__init.sql` creates all MVP tables
- `.env.example` committed, `.env` in `.gitignore`

### Issues

| # | Issue | Owner | Key labels |
|---|---|---|---|
| 1 | `chore(repo): initialise repository structure & docker-compose skeleton` | BE | `area:devops` `sprint:1` `priority:critical` |
| 2 | `chore(docker): configure nginx reverse-proxy with HTTPS` | BE | `area:devops` `area:security` `sprint:1` |
| 3 | `chore(db): initial PostgreSQL schema & Flyway migrations` | BE (TL) | `area:db` `sprint:1` `priority:critical` |
| 4 | `feat(backend): Spring Boot scaffold with security config` | BE | `area:backend` `sprint:1` `mandatory` |
| 5 | `feat(frontend): Angular scaffold with routing & Tailwind CSS` | FE | `area:frontend` `sprint:1` `mandatory` |
| 6 | `docs(readme): write initial README with all required sections` | PM | `sprint:1` `mandatory` |

### MVP database tables

```
users           id · login · email · password_hash · avatar_url · elo · created_at
friendships     user_id · friend_id · status
challenges      id · title · description · difficulty · time_limit_secs · test_cases (JSON)
duels           id · challenger_id · opponent_id · challenge_id · status · started_at · ended_at
submissions     id · duel_id · user_id · language · code · score · submitted_at
rankings        user_id · elo · league · win_streak
notifications   id · user_id · type · payload · read · created_at
messages        id · sender_id · recipient_id · content · created_at
```

### Module point milestone
After Sprint 1: **2 pts** (frameworks)

### Capacity check
6 issues · estimated ~55h · within ~64h team capacity ✓

---

## 6. Sprint 2 — Core Backend & Auth (Mar 18 → Apr 1)

**Goal**: A user can register, log in, view and edit their profile, and browse the challenge list.

**Tuesday Apr 1 exit criteria**:
- Full auth flow works in browser (register → login → profile page)
- Profile editable with avatar upload
- Challenge list renders with difficulty filter
- All forms have frontend + backend validation

### Issues

| # | Issue | Owner | Key labels |
|---|---|---|---|
| 7 | `feat(auth): JWT register, login, refresh & logout endpoints` | BE | `area:auth` `area:backend` `sprint:2` `module:major` `mandatory` |
| 8 | `feat(user): user profile CRUD endpoints & avatar upload` | BE | `area:backend` `sprint:2` `module:major` `mandatory` |
| 9 | `feat(frontend): login, register & profile pages` | FE | `area:frontend` `sprint:2` `mandatory` |
| 10 | `feat(challenge): entity, 20-challenge seed data & CRUD API` | BE (TL) | `area:backend` `area:db` `sprint:2` `mandatory` |
| 11 | `feat(frontend): challenge list & detail page with Monaco (read-only)` | FE | `area:frontend` `sprint:2` |

### Module point milestone
After Sprint 2: **4 pts** cumulative (+ user management)

### Capacity check
5 issues · estimated ~52h · within ~64h team capacity ✓

---

## 7. Sprint 3 — Game Engine & Real-time (Apr 1 → Apr 15)

> ⚠ **This is the most technically difficult sprint.** It delivers 7 of the 16 mandatory module
> points and contains the most novel engineering. Scope is intentionally lean — protect it
> from additions.

**Goal**: Two users in separate browser tabs can queue, be matched, play a duel, and see a result — even if the UI is rough.

**Tuesday Apr 15 exit criteria**:
- User A and User B enter the ranked queue and are matched
- Both receive the same challenge in their Monaco editor
- Code submitted is executed in the Docker sandbox
- Results compared, winner determined, Elo updated
- Match history visible on profile page

### Issues

| # | Issue | Owner | Key labels |
|---|---|---|---|
| 12 | `feat(judge): Docker sandbox code execution service` | BE (TL) | `area:judge` `area:security` `sprint:3` `module:major` `priority:critical` |
| 13 | `feat(websocket): STOMP/SockJS WebSocket infrastructure` | BE | `area:realtime` `area:backend` `sprint:3` `module:major` `priority:critical` |
| 14 | `feat(matchmaking): Elo-based ranked queue with Redis` | BE | `area:backend` `sprint:3` `priority:critical` |
| 15 | `feat(duel): duel lifecycle state machine (WAITING → COMPLETED)` | BE (TL) | `area:backend` `sprint:3` `module:major` `priority:critical` |
| 16 | `feat(ranking): Elo calculation & league assignment` | BE | `area:backend` `sprint:3` `module:minor` `mandatory` |
| 17 | `feat(frontend): lobby page & matchmaking queue UI` | FE | `area:frontend` `sprint:3` |
| 18 | `feat(frontend): Arena page – Monaco editor, timer & opponent status` | FE | `area:frontend` `sprint:3` `module:major` `priority:critical` |

### Judge sandbox security requirements (non-negotiable)

Every submission runs in an isolated container with all of the following:

```
--cpus=0.5                          hard CPU cap
--memory=128m                       hard RAM cap
--network=none                      no outbound network
--read-only                         read-only filesystem
--tmpfs /tmp                        writable temp only
--security-opt=no-new-privileges    no privilege escalation
hard kill after 10s                 enforced by JudgeService (not container timeout)
--rm                                auto-remove after execution
```

### Duel scoring formula

```
FinalScore = 0.40 × TimeScore
           + 0.30 × PerfScore
           + 0.20 × CorrectnessBonus
           + 0.10 × CodeQualityScore

TimeScore   = (max_time - submission_time) / max_time × 100
PerfScore   = (1000 / runtime_ms) × memory_bonus
```

### Sprint 3 mitigation plan

If the sprint is running tight at the mid-point (Wednesday Apr 8):

- **If Judge sandbox is behind**: simplify to pass/fail only (no performance metrics) — earn the module point, add metrics in Sprint 4
- **If Arena UI is behind**: deliver a minimal version that shows the Monaco editor and a submit button — polish moves to Sprint 4
- **Do not descope the matchmaking or duel engine** — these are required for the remote players module point

### Module point milestone
After Sprint 3: **11 pts** cumulative — the critical threshold.

### Capacity check
7 issues · estimated ~70h · slightly over nominal capacity (intentional — Sprint 3 is the bottleneck sprint)

---

## 8. Sprint 4 — Frontend, Modules & Evaluation Prep (Apr 15 → Apr 29)

**Goal**: All 16 mandatory module points accepted. Application is evaluation-ready. Every team member can explain the whole project.

**Tuesday Apr 29 exit criteria**:
- Leaderboard renders with real Elo data
- Chat works between two logged-in users in real-time
- Notifications appear for duel results and friend requests
- Gamification system live (3+ achievement types, XP on profile)
- ORM module documented in README
- `docker compose up --build` succeeds from a fresh clone
- Zero console errors in Chrome DevTools
- No secrets in git history
- README complete and reviewed by the whole team

### Issues — Feature completion

| # | Issue | Owner | Key labels |
|---|---|---|---|
| 19 | `feat(frontend): ranking leaderboard & profile stats page` | FE | `area:frontend` `sprint:4` `mandatory` |
| 20 | `feat(chat): real-time direct messaging (Module: User Interaction)` | FE + BE | `area:frontend` `area:backend` `area:realtime` `sprint:4` `module:major` `mandatory` |
| 21 | `feat(notifications): notification system (Module: Minor)` | FE + BE | `area:frontend` `area:backend` `sprint:4` `module:minor` `mandatory` |
| 22 | `chore(orm): confirm Hibernate/JPA coverage & document (Module: Minor)` | BE (TL) | `area:backend` `area:db` `sprint:4` `module:minor` `mandatory` |
| 23 | `feat(gamification): achievements, badges & XP system (Module: Minor)` | BE + FE | `area:backend` `area:frontend` `sprint:4` `module:minor` `mandatory` |
| 24 | `feat(frontend): Privacy Policy & Terms of Service pages` | FE | `area:frontend` `sprint:4` `mandatory` |
| 25 | `fix(frontend): mobile responsiveness pass (375px–1280px)` | FE | `area:frontend` `sprint:4` |

### Issues — Hardening & evaluation prep

| # | Issue | Owner | Key labels |
|---|---|---|---|
| 26 | `test(security): full audit – XSS, SQLi, CORS, JWT, file upload` | TL | `area:security` `sprint:4` `priority:critical` |
| 27 | `chore(env): .env audit – no secrets in repository history` | PM | `area:security` `area:devops` `sprint:4` `priority:critical` |
| 28 | `fix(frontend): Chrome console – zero errors & warnings pass` | FE | `area:frontend` `sprint:4` `priority:critical` |
| 29 | `chore(deploy): single-command clean deploy smoke test` | BE | `area:devops` `sprint:4` `priority:critical` |
| 30 | `docs(readme): final README review & evaluation prep` | PM | `sprint:4` `priority:critical` |
| 31 | `chore(git): git history audit – commits from all members` | PM | `area:devops` `sprint:4` `priority:high` |

### Issues — Bonus (only if mandatory is fully done)

| # | Issue | Owner | Key labels |
|---|---|---|---|
| 32 | `feat(bonus): OAuth 2.0 login with GitHub / 42 (+1pt)` | BE | `area:auth` `sprint:4` `bonus` `module:minor` |
| 33 | `feat(bonus): spectator mode for live duels (+1pt)` | BE + FE | `area:realtime` `sprint:4` `bonus` `module:minor` |

### Security audit checklist (issue 26)

| Check | Method | Pass? |
|---|---|---|
| XSS | Manual: `<script>alert(1)</script>` in all text inputs | — |
| SQL injection | JPA parameterised queries only; review any native queries | — |
| CORS | Restricted to known origins in Spring Security config | — |
| File upload | Type (image only), size (≤2MB), format validated server-side | — |
| JWT | Signature verified, expiry checked on every protected endpoint | — |
| Sensitive data in responses | No passwords, tokens, or secrets in any API response or log | — |
| No secrets in codebase | `git grep -rn 'password\|secret\|apikey'` | — |
| No `.env` in history | `git log --all -- '*.env'` returns nothing | — |
| HTTPS everywhere | Nginx config + Spring Security config | — |
| Chrome console clean | Manual walkthrough of all main user flows | — |

### Presentation Readiness (all team members)

The following should be explainable without supplementary notes:

**Individual Work**
- What is the role? What specific features were built?
- Demonstration of a feature from DB schema to UI.
- Identification of the most significant problem solved and the implementation details of the solution.

**Project Knowledge**
- Functionality of Code Arena and the end-to-end duel lifecycle.
- Rationale for the selection of Angular, Spring Boot, PostgreSQL, and Redis.
- Mechanism by which the Docker sandbox isolates code execution.
- Functionality of the Elo ranking system.
- Team organization and methodology.

**About the modules**
- Which modules were implemented? (preparation for live demonstration is required)
- How is each module justified in the README?

### Module point milestone
After Sprint 4: **16 pts mandatory** (+ up to 2 bonus if capacity allowed)

### Capacity check
Sprint 4 is deliberately heavy: 13 mandatory issues + 2 optional bonus. The hardening issues (26–31) run in parallel with the feature issues (19–25) — they are not sequential. Assign hardening issues to whoever finishes their feature work first. The PM tracks both tracks simultaneously.

---

## 9. Risk Register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Sprint 3 overruns — Judge sandbox or duel engine takes longer than expected | **High** | **High** | Mitigation plan built into Sprint 3 (see above). Stub the Arena UI if needed; backend functional earns the module points |
| Team member has reduced capacity for a sprint | Medium | Medium | Announce at planning; PM re-sizes the backlog. 20% buffer absorbs one person's bad week |
| 14-point threshold at risk after Sprint 3 | Low | Critical | 2-point mandatory buffer built in. Emergency scope session if cumulative < 11 on Apr 15 |
| Frontend and backend built on incompatible API assumptions | Medium | Medium | API contracts agreed at sprint planning before work starts; recorded in the issue |
| Secrets accidentally committed | Low | Critical | `.gitignore` enforced from Sprint 1; pre-PR checklist; dedicated audit issue in Sprint 4 |
| Clean deploy fails on evaluation day | Medium | High | Sprint 4 smoke test done on a fresh VM, not just the dev machine |
| Team member cannot explain their contribution at evaluation | Low | High | Sprint reviews require each person to demo their own work; oral prep is a Sprint 4 issue |
| Bonus modules displace mandatory hardening | Medium | High | Bonus issues start only after all 16 mandatory points are accepted in Sprint 4 review |

---

## 10. Evaluation Readiness Checklist

Use this as the final gate before evaluation. Every item must be checked.

### General requirements
- [ ] Frontend, backend, and database all running from a fresh clone
- [ ] `docker compose up` starts everything with a single command
- [ ] Application accessible on `https://localhost`
- [ ] Zero console errors in latest stable Chrome
- [ ] Privacy Policy page — accessible, real content
- [ ] Terms of Service page — accessible, real content
- [ ] Multiple users can be logged in and active simultaneously

### Technical requirements
- [ ] Frontend responsive at 375px (mobile) and 1280px (desktop)
- [ ] Tailwind CSS used throughout
- [ ] `.env.example` committed; `.env` in `.gitignore`
- [ ] Database has a documented schema
- [ ] Passwords hashed and salted (BCrypt minimum)
- [ ] All forms validated in both frontend and backend
- [ ] All backend traffic uses HTTPS

### Git requirements
- [ ] All four team members have commits visible on `main`
- [ ] Commit messages follow Conventional Commits throughout
- [ ] No secrets or `.env` files in git history
- [ ] `git shortlog -sn --no-merges` shows reasonable distribution

### README requirements
- [ ] First line italicised with all team logins
- [ ] Description — project name and key features
- [ ] Instructions — prerequisites, `.env` setup, step-by-step start
- [ ] Resources — references + AI usage disclosure
- [ ] Team Information — roles and responsibilities
- [ ] Project Management — tools, meetings, communication
- [ ] Technical Stack — with justifications
- [ ] Database Schema — visual or textual
- [ ] Features List — with owners
- [ ] Modules — list, point totals, justifications, implementations
- [ ] Individual Contributions — detailed per team member

### Module validation (each must be demonstrable live)

| Module | Points | Ready? |
|---|---|---|
| Angular + Spring Boot frameworks | 2 | — |
| Standard user management | 2 | — |
| Real-time WebSockets | 2 | — |
| Complete web-based game (Code Duel) | 2 | — |
| Remote players | 2 | — |
| Game statistics & match history | 1 | — |
| User interaction (chat + profile + friends) | 2 | — |
| ORM (Hibernate/JPA) | 1 | — |
| Notification system | 1 | — |
| Gamification | 1 | — |
| **Total: 16 pts** | | |

### Oral readiness
- [ ] Each team member can explain their role and 2+ specific contributions
- [ ] Each team member can explain the overall architecture
- [ ] Each team member can explain the modules they built
- [ ] Team has practised the live code modification scenario

---

*This roadmap is a living document. Update key dates, capacity notes, and module status after every sprint review (Tuesday Week 2).*