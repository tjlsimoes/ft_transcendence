# Scrum Methodology — Code Arena
> ft_transcendence · 4-person team · ~8 weeks · ~8–10h/week per person · Tuesday meetings

---

## Table of Contents

1. [Why Scrum?](#1-why-scrum)
2. [Team Roles](#2-team-roles)
3. [Sprint Structure](#3-sprint-structure)
4. [The Tuesday Meeting](#4-the-tuesday-meeting)
5. [Ceremonies](#5-ceremonies)
   - [Combined Sprint Event (Tuesday)](#51-combined-sprint-event-tuesday)
   - [Daily Standup (Async)](#52-daily-standup-async)
   - [Backlog Refinement](#53-backlog-refinement)
6. [Artefacts](#6-artefacts)
   - [Product Backlog](#61-product-backlog)
   - [Sprint Backlog](#62-sprint-backlog)
   - [Definition of Done](#63-definition-of-done)
7. [Capacity & Realistic Expectations](#7-capacity--realistic-expectations)
8. [The PM/Scrum Master Playbook](#8-the-pmscrum-master-playbook)
9. [Evaluation Alignment](#9-evaluation-alignment)

---

## 1. Why Scrum?

Code Arena has characteristics that make a lightweight Scrum a natural fit:

| Project characteristic | Why Scrum helps |
|---|---|
| Soft end-of-April target | Sprints create intermediate checkpoints so problems surface in week 4, not week 7 |
| Modular grading (14+ pts) | Each module maps to a sprint goal; point progress is trackable after every review |
| Parallel frontend/backend workstreams | Sprint planning forces coordination contracts before either side starts building |
| Part-time, distributed team | A single weekly sync keeps everyone aligned without requiring daily availability |
| Mandatory team-wide understanding at evaluation | Shared ceremonies keep everyone informed about the whole project, not just their slice |

### What we adapt

Full Scrum assumes dedicated, full-time teams with separate planning, review, retro, and refinement sessions. With 8–10 hours per week per person and a single 1-hour Tuesday meeting, we adapt aggressively:

- **All ceremonies collapse into one Tuesday hour.** Planning on Week 1 Tuesday; review + retro on Week 2 Tuesday.
- **Standups are async** — a short Discord post, not a call.
- **Backlog refinement is a PM solo task**, not a team ceremony, protecting everyone's limited hours.
- **4 sprints of 2 weeks each**, March 4 → April 29.

The goal is not process purity. It is shipping 14+ validated module points and being able to defend every line of code — while respecting that everyone has other commitments.

---

## 2. Team Roles

| Person | Scrum Role | Project Role | Primary area |
|---|---|---|---|
| A | Product Owner | PO + Developer | Frontend |
| B | Scrum Master / PM | PM + Developer | Backend |
| C | Developer | Tech Lead / Architect + Developer | Backend |
| D | Developer | Developer | Frontend |

> Roles overlap by design. Role titles describe *primary responsibilities*, not exclusive domains.
> Everyone commits code every sprint.

### Product Owner (A)
- Owns and prioritises the product backlog
- Writes or approves acceptance criteria before an issue enters a sprint
- Makes the final call at sprint review: does this feature meet the requirement?
- Is **not** responsible for tracking day-to-day progress — that is the PM

### Scrum Master / PM (B)
- Prepares the Tuesday agenda and keeps it on time
- Maintains the GitHub Project board as the single source of truth
- Responds to blockers within 24h and ensures they are addressed
- Tracks module point progress after each sprint review
- Is **not** a manager — does not assign every task or evaluate teammates

### Tech Lead (C)
- Makes final calls on architecture and technology choices
- Reviews all PRs touching the DB schema, auth, the Judge sandbox, or WebSocket infrastructure
- Records architectural decisions in GitHub Discussions so everyone can answer at evaluation
- Pairs with teammates on difficult problems rather than solving everything alone

### Developer (all four)
- Implement features assigned at sprint planning
- Write tests for their own work
- Review at least one PR per sprint
- Post async standups and keep their board issues updated

---

## 3. Sprint Structure

The project runs **4 two-week sprints**, anchored on Tuesdays.

```
Sprint 1  │ Mar  4 → Mar 18  │ Foundation
Sprint 2  │ Mar 18 → Apr  1  │ Core Backend & Auth
Sprint 3  │ Apr  1 → Apr 15  │ Game Engine & Real-time  ⚠ hardest sprint
Sprint 4  │ Apr 15 → Apr 29  │ Frontend, Modules & Evaluation Prep
```

Each sprint has two Tuesdays:

```
Tuesday Week 1  →  Sprint Planning       (sprint begins)
                   [work, work, work]
Tuesday Week 2  →  Review + Retro        (sprint ends, next begins immediately)
```

Work should be wrapped up by **end of Monday** before the Week 2 Tuesday — not Tuesday morning.

### Why 4 sprints is tight but realistic

At 8–10h/week per person, each 2-week sprint gives roughly **64–80h of total team capacity**. Four sprints = 256–320h total. That is enough to deliver 16 module points if scope is protected sprint by sprint. The constraint is that there is almost no slack — a sprint that overruns cannot simply be absorbed by adding a fifth sprint. Scope discipline matters more here than in a longer project.

---

## 4. The Tuesday Meeting

**Duration**: 60 minutes online
**Who**: All four team members — attendance expected every week
**Facilitator**: SM/PM

### Tuesday Week 1 — Sprint Planning (60 min)

| Time | Activity |
|---|---|
| 0–5 min | PM presents the sprint goal and pre-groomed candidate issues |
| 5–30 min | Team reviews each issue: acceptance criteria clear? Unknowns? Dependencies? |
| 30–50 min | Team commits to the sprint backlog — issues moved to "To Do", owners assigned |
| 50–60 min | Flag cross-team API contracts, confirm any pair-working, note dependencies |

### Tuesday Week 2 — Review + Retro (60 min)

| Time | Activity |
|---|---|
| 0–5 min | PM updates the board live: confirmed Done vs carries over |
| 5–30 min | Sprint Review — each developer demos their work against acceptance criteria |
| 30–40 min | PO accepts or rejects each item (binary — "mostly done" = not done) |
| 40–45 min | PM announces cumulative module point total |
| 45–60 min | Retrospective — Start / Stop / Continue, max 2 action items |

### Keeping to 60 minutes

- **No surprises at review.** If something will not be done, say so in `#standup` before Tuesday.
- **Demos are focused.** Show the feature working against its acceptance criteria — not a code tour.
- **Retro is brief.** One item per person per column. Two concrete action items only.
- **Decisions needing more than 5 minutes** are parked to a GitHub Discussion or a short separate call.

---

## 5. Ceremonies

### 5.1 Combined Sprint Event (Tuesday)

All Scrum ceremonies fit within the single Tuesday hour because:

1. The PM grooms the backlog before the meeting — planning is fast when issues are already well-defined
2. Each developer demos their own work — no shared demo to coordinate or prepare
3. The retro is kept to its minimum viable form

### 5.2 Daily Standup (Async)

**When**: Any working day you touch the project — posted in `#standup` on Discord
**Format**: Three lines, posted before noon

```
✅ Yesterday: [what I completed or progressed — issue # if possible]
🔨 Today: [what I plan to work on]
🚧 Blocked: [anything blocking me, or "nothing"]
```

**Realistic expectations for a part-time team:**

- Post on days you work on the project. "No project work yesterday" is honest and fine — silence is not.
- If you have not posted for 3+ consecutive days, a brief check-in is still appreciated.
- If someone is blocked, the SM responds within 24h on working days.
- If two or more people are blocked on the same thing, that becomes a short call — not an async thread.

The standup is a **coordination tool for teammates**, not a status report.

### 5.3 Backlog Refinement

**When**: Wednesday or Thursday of Week 1 of each sprint (PM's own time)
**Who**: PM solo, or briefly with PO if architectural questions arise
**Duration**: ~30 minutes

This is PM preparation work that makes Tuesday planning fast. Without it, the meeting becomes 60 minutes of "what does this issue even mean?"

**What to do:**
- Review issues planned for the next sprint: are acceptance criteria specific and testable?
- Split issues that are too large (more than ~10h of one person's time)
- Apply labels, milestone, and area tags to ungroomed issues
- Confirm module point mapping is accurate
- Flag cross-team dependencies that need a conversation at planning

---

## 6. Artefacts

### 6.1 Product Backlog

**Location**: GitHub Project board — "Backlog" column
**Owner**: Product Owner (with PM maintaining hygiene)

Every piece of work lives here as a GitHub Issue before entering a sprint. Nothing is worked on without an issue.

**Sprint-ready checklist:**
- [ ] Title follows Conventional Commits format
- [ ] Goal explains the *why*
- [ ] Acceptance criteria are specific and binary (pass/fail)
- [ ] Labels: `area:*`, `sprint:*`, `priority:*`, `module:*` if applicable
- [ ] Milestone (sprint) assigned
- [ ] Dependencies noted

### 6.2 Sprint Backlog

**Location**: GitHub Project board — "To Do" and "In Progress" columns
**Owner**: The whole team during the sprint

The sprint backlog is **locked** once planning ends. On a 4-sprint project, mid-sprint additions have nowhere to go — they displace mandatory work. New issues only enter mid-sprint if the PO explicitly removes something of equivalent weight.

**Acceptable mid-sprint adjustments** (PM + PO agreement required):
- A blocker makes an issue undeliverable → move it back to backlog with a note
- A critical bug blocks other work → swap it in, displace the lowest-priority issue
- An issue proves much larger than expected → split it, carry the remainder to the next sprint

### 6.3 Definition of Done

An issue is **Done** only when all of the following are true. No partial credit.

#### Code
- [ ] Reviewed and approved by at least one teammate
- [ ] No commented-out debug code or open `TODO`s in the diff
- [ ] No magic strings or numbers without named constants
- [ ] Follows project conventions (naming, package structure, Conventional Commits)

#### Testing
- [ ] Manually tested against every acceptance criterion
- [ ] Unit or integration tests written for non-trivial backend logic
- [ ] No new console errors in Chrome DevTools

#### Integration
- [ ] PR merged to `main` via squash-and-merge
- [ ] `docker compose up --build` still works after the merge
- [ ] No regressions in previously working features

#### Documentation
- [ ] README updated if the feature affects setup, tech stack, or the modules list
- [ ] DB schema updated if a migration changed the schema

#### Security (backend and auth features)
- [ ] All user inputs validated in both frontend and backend
- [ ] No credentials introduced into the codebase
- [ ] HTTPS used for all new endpoints

---

## 7. Capacity & Realistic Expectations

### Per-sprint capacity

| Availability | Hours/week | Hours per sprint | Minus meetings & reviews | Effective coding hours |
|---|---|---|---|---|
| Conservative | 8h | 16h | ~2h | **~14h** |
| Moderate | 10h | 20h | ~2h | **~18h** |

**Team capacity per sprint: ~64h** (4 people × conservative estimate)

### Issue sizing guide

| Issue type | Estimated hours |
|---|---|
| Simple backend endpoint | 2–4h |
| Full feature: backend + frontend + tests | 6–10h |
| Complex feature: Judge sandbox, WebSocket infra | 10–15h |
| Infrastructure / Docker / config | 3–6h |
| Documentation | 1–3h |

Expect **6–8 completed issues per sprint** across the whole team. Plan to that number — not to an optimistic upper bound.

### The 4-sprint constraint

With only 4 sprints, there is no sprint to "catch up" in. Every sprint that ends with significant unfinished work pushes the problem forward and compresses the one after it. The only viable response to over-commitment is better planning, not longer hours.

**Rules that protect capacity:**
- Leave ~20% unplanned headroom — roughly 1–2 issues worth — to absorb the unexpected
- Flag reduced availability at sprint planning (exams, deadlines, trips)
- Bonus modules are picked up only after all mandatory module points are confirmed done
- "Mostly done" is not done — it goes back to the backlog

---

## 8. The PM/Scrum Master Playbook

### Week 0 — Before March 4

- [ ] Run `setup_github_project.sh` to create the board, issues, labels, and milestones
- [ ] Set up Discord: `#standup`, `#general`, `#blockers`, `#decisions`
- [ ] Confirm with the team: realistic hours per week, any known unavailable weeks in March–April
- [ ] Hold a brief kick-off: walk through the roadmap, confirm roles, set the sprint calendar
- [ ] Confirm everyone can clone the repo and see it build

### Every Tuesday Week 1 (planning day)

- [ ] Have the backlog groomed by Monday
- [ ] Share the GitHub board on screen during the call
- [ ] After the meeting: move committed issues to "To Do", post the sprint goal in `#general`

### Mid-sprint (Wednesday or Thursday of Week 1)

- [ ] Scan the board: any "In Progress" issues with no recent commit activity?
- [ ] Check `#standup`: anyone silent for 3+ days?
- [ ] Groom the next sprint's backlog (~30 min)
- [ ] Follow up on any `status:blocked` issues

### Every Tuesday Week 2 (review + retro day)

- [ ] Before the call: update the board, calculate module points earned this sprint
- [ ] Facilitate review (demos) and retro (Start / Stop / Continue)
- [ ] After the call: move incomplete issues back to backlog, write retro action items into next sprint's planning notes

### Module point tracker

| Module | Points | Sprint | Status |
|---|---|---|---|
| Frameworks (Angular + Spring Boot) | 2 | S1 | 🔴 |
| Standard User Management | 2 | S2 | 🔴 |
| Real-time WebSockets | 2 | S3 | 🔴 |
| Complete web-based game | 2 | S3 | 🔴 |
| Remote players | 2 | S3 | 🔴 |
| Game Statistics & Match History | 1 | S3 | 🔴 |
| User Interaction (chat + profile + friends) | 2 | S4 | 🔴 |
| ORM (Hibernate/JPA) | 1 | S4 | 🔴 |
| Notification System | 1 | S4 | 🔴 |
| Gamification | 1 | S4 | 🔴 |
| **Total mandatory** | **16** | | |
| OAuth 2.0 *(bonus — capacity permitting)* | +1 | S4 | 🔴 |
| Spectator Mode *(bonus — capacity permitting)* | +1 | S4 | 🔴 |

🔴 Not started · 🟡 In progress · 🟢 Accepted in review

> ⚠ If cumulative total is below **11 points after the Sprint 3 review (Apr 15)**,
> run an emergency scope session with the PO before Sprint 4 planning begins.

### Escalation guide

| Situation | Action |
|---|---|
| Someone blocked for 24h+ | DM directly; involve TL if technical |
| Issue stuck "In Progress" for 5+ days | Check in — silently stuck or actually progressing? |
| Points below 11 after Sprint 3 | Emergency scope session with PO — what gets cut or simplified? |
| Architectural decision buried in a PR comment | Move to `#decisions` or GitHub Discussion immediately |
| Consistent over-commitment sprint after sprint | Reduce the next backlog — do not let the pattern repeat |

---

## 9. Evaluation Alignment

| Evaluation check | How our process covers it |
|---|---|
| All members present and can explain their role | Each person owns their issues and demos their own work at sprint review |
| README complete | README updates are part of the Definition of Done for every feature |
| Git history shows commits from all members | Issues assigned individually at planning; PRs enforce authorship |
| Each module demonstrated and functional | Only DoD-complete items count; PM tracks per-module status every sprint |
| Team can explain technical choices | Decisions recorded in GitHub Discussions; everyone attends Tuesday meetings |
| No console errors in Chrome | Part of DoD; Sprint 4 has a dedicated audit issue |
| Single-command deployment | Part of DoD for all infra issues; Sprint 4 includes a clean-deploy smoke test |
| Privacy Policy and Terms of Service | Explicit Sprint 4 issue — not left to the last week |

---

*Last updated: Sprint 0 (Mar 4). Revisit at the Sprint 4 retrospective and update anything that changed significantly.*