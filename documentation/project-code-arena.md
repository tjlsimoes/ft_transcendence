# Code Arena — PvP Code Duel with Elo Rating System

> PvP platform inspired by Codewars/LeetCode with a ranked ladder for ft_transcendence.
> Players compete in duels, receive identical coding challenges, and the winner is awarded LP (League Points).

---

## 1. Concept

A web platform where participants duel in real time to solve coding challenges.
The ranking system (Elo) increases challenge difficulty as progress is made.
Inspired by Codewars, LeetCode, and the competitive systems of games like League of Legends.

---

## 2. Tech Stack

| Layer        | Technology                          | Justification                                              |
|--------------|-------------------------------------|------------------------------------------------------------|
| Frontend     | **Angular**                         | Robust framework, excellent for complex SPAs               |
| Backend      | **Java Spring Boot**                | Mature ecosystem, built-in security, scalable              |
| Database     | **PostgreSQL**                      | Relational, robust for rankings and user relationships     |
| ORM          | **Hibernate / Spring Data JPA**     | Native integration with Spring Boot                        |
| Real-Time    | **WebSockets (STOMP + SockJS)**     | Real-time duels, chat, and spectator                       |
| Editor       | **Monaco Editor** (VS Code engine)  | In-browser code editor with syntax highlighting            |
| Sandbox      | **Docker containers**               | Execute user code in an isolated and secure manner          |
| Cache/Queue  | **Redis**                           | Matchmaking queue, sessions, ranking cache                 |
| CSS          | **Angular Material / Tailwind CSS** | Responsive and consistent UI                               |

---

## 3. Core Features

### 3.1 Matchmaking
- Participation in the ranked queue is initiated.
- The system identifies an opponent with a similar Elo (±200 points).
- Queue timeout: 60s → the Elo range is progressively expanded.

### 3.2 Duel
1. Both participants receive the **same challenge** simultaneously.
2. The code editor (Monaco) features a visible timer.
3. Submissions trigger automated tests within a Docker sandbox.
4. Final results are compared upon completion or dual submission.

### 3.3 Time Limit by Difficulty

| Difficulty | Time   | Elo Range     |
|------------|--------|---------------|
| Easy       | 5 min  | Bronze        |
| Medium     | 10 min | Silver        |
| Hard       | 20 min | Gold          |
| Insane     | 30 min | Master+       |

### 3.4 Ranking System (Elo / Glicko-2)

| League   | LP Range       | Color     |
|----------|----------------|-----------|
| Bronze   | 0 – 999        | Brown     |
| Silver   | 1000 – 1999    | Gray      |
| Gold     | 2000 – 2999    | Golden    |
| Master   | 3000+          | Purple    |
| Legend   | Top 1% Masters | Red       |

- LP gain/loss based on the Elo difference between opponents
- Win streak grants a bonus
- Inactivity decay (optional)
- **Master** has no LP ceiling — Legend is reserved for the **top 1%** of all Master+ players
- **Master UI**: shows current LP + LP threshold of the last-placed Legend player (promotion target)
- **Legend UI**: shows current LP + current global ranking position + highest LP among all players

---

## 4. Duel Evaluation Criteria

### 4.1 Correctness (eliminatory)
- Does the code pass **all** test cases?
- Yes → proceeds to the next criteria
- No → **automatic defeat**

### 4.2 Submission Time (weight: 40%)
- Whoever submits first gains an advantage
- Formula: `TimeScore = (max_time - submission_time) / max_time * 100`

### 4.3 Performance / Efficiency (weight: 30%)
- Code executed against tests with large inputs
- Metrics collected via sandbox:
  - **Execution time** (ms)
  - **Memory usage** (MB)
- Formula: `PerfScore = (1000 / runtime_ms) * memory_bonus`

### 4.4 Partial Correctness — Bonus (weight: 20%)
- % of test cases passed (if neither passes all)
- Tiebreaker when both pass all: fixed bonus

### 4.5 Code Quality (weight: 10%)
- Lines of code (fewer = better, with minimum threshold)
- Light static analysis (optional): cyclomatic complexity, lint warnings

### Final Formula
```
FinalScore = 0.40 * TimeScore
           + 0.30 * PerfScore
           + 0.20 * CorrectnessBonus
           + 0.10 * CodeQualityScore
```
**Highest score wins the duel.**

---

## 5. System Architecture

```
┌─────────────┐       WebSocket/REST       ┌──────────────────┐
│   Angular    │ ◄──────────────────────► │  Spring Boot API  │
│  (Frontend)  │                           │   (Backend)       │
└─────────────┘                           └────────┬─────────┘
                                                    │
                            ┌───────────────────────┼──────────────────┐
                            │                       │                  │
                     ┌──────▼──────┐     ┌──────────▼───┐    ┌────────▼────────┐
                     │ PostgreSQL  │     │    Redis     │    │ Docker Sandbox  │
                     │ (Database)  │     │ (Queue/Cache)│    │ (Code Runner)   │
                     └─────────────┘     └──────────────┘    └─────────────────┘
```

### Backend — Main Services

| Service              | Responsibility                                           |
|----------------------|----------------------------------------------------------|
| AuthService          | JWT, login, registration, OAuth (optional)               |
| MatchmakingService   | Ranked queue, Elo-based pairing                          |
| DuelService          | Manage duel state, timer, submissions                    |
| JudgeService         | Execute code in Docker sandbox, collect metrics          |
| RankingService       | Elo/Glicko-2 calculation, match history                  |
| ChallengeService     | CRUD for challenges, categorization by difficulty & tags |
| UserService          | Profile, avatar, stats, friends                          |
| NotificationService  | Duel notifications, results, ranking                     |
| WebSocketService     | Real-time communication (duel, chat, spectator)          |

### Frontend — Main Pages/Components

| Screen          | Description                                             |
|-----------------|---------------------------------------------------------|
| Home            | Dashboard with ranking, personal stats, quick queue     |
| Lobby           | Matchmaking queue, search status                        |
| Arena           | Code editor + timer + tests + opponent status           |
| Result          | Detailed comparison of solutions                        |
| Profile         | Stats, Elo, win rate, match history                     |
| Ranking         | Global leaderboard, league filters                      |
| Challenges      | Problem list for practice (outside ranked)              |
| Chat            | Messaging system between friends                        |

---

## 6. Mapping to ft_transcendence Modules

### Goal: reach a minimum of 14 points

| Module                                                          | Type  | Pts | Justification                                                     |
|-----------------------------------------------------------------|-------|-----|-------------------------------------------------------------------|
| **Web: Frontend + backend framework (Angular + Spring Boot)**   | Major | 2   | Project foundation                                                |
| **Web: Real-time with WebSockets**                              | Major | 2   | Real-time duels, matchmaking, spectator                           |
| **Web: User interaction (chat + profile + friends)**            | Major | 2   | Chat, player profile, friends system                              |
| **Gaming: Complete web game**                                   | Major | 2   | The code duel IS the main game                                    |
| **Gaming: Remote players**                                      | Major | 2   | Two players on separate computers dueling                         |
| **User Management: Standard (profile, avatar, friends, status)**| Major | 2   | Full profile with stats, avatar, online status                    |
| **User Management: Game statistics and match history**          | Minor | 1   | Duel history, win rate, ranking, progression                      |
| **Web: ORM (Hibernate/JPA)**                                    | Minor | 1   | Integrated Spring Data JPA                                        |
| **Web: Notification system**                                    | Minor | 1   | Duel, result, friend request notifications                        |
| **Gaming: Gamification (achievements, leaderboard, XP)**        | Minor | 1   | League system, badges, win streak                                 |
|                                                                 |       |     |                                                                   |
| **TOTAL**                                                       |       | **16** | **2-point margin above the minimum of 14**                     |

### Extra Modules (Bonus — up to 5 additional pts)

| Module                                                   | Type  | Pts | Justification                                |
|----------------------------------------------------------|-------|-----|------------------------------------------------------|
| Gaming: Spectator mode                                   | Minor | 1   | Watch duels live                                     |
| Gaming: Tournament system                                | Minor | 1   | Tournaments with brackets                            |
| AI: AI Opponent (Ghost Battles)                          | Major | 2   | Fight against top players' solutions                 |
| User Management: OAuth 2.0                               | Minor | 1   | Login with GitHub/Google/42                          |

---

## 7. Security — Executing User Code

**This is the most critical aspect of the project.**

### Risks
- Fork bomb, infinite loops
- Host filesystem access
- Network access
- Excessive CPU/RAM consumption

### Solution: Docker Sandbox

| Measure               | Implementation                                          |
|-----------------------|---------------------------------------------------------|
| Isolated container    | Each submission runs in an ephemeral container          |
| CPU limit             | `--cpus=0.5` (half a core)                              |
| RAM limit             | `--memory=128m`                                         |
| Time limit            | Hard kill after timeout (e.g., 10s execution)           |
| No network            | `--network=none`                                        |
| Read-only filesystem  | `--read-only` + tmpfs for /tmp                          |
| Seccomp profile       | Restrict dangerous syscalls                             |
| No privileges         | `--security-opt=no-new-privileges`                      |

---

## 8. Pros

| #  | Advantage                                                                             |
|----|---------------------------------------------------------------------------------------|
| 1  | **Very impressive** for evaluation — demonstrates real-time, security, gamification   |
| 2  | **Covers many modules** naturally (game, WebSocket, user management, etc.)            |
| 3  | **Strong portfolio** — project with real product appeal (FAANG-level)                 |
| 4  | **Original** — few do something like this in ft_transcendence                         |
| 5  | **Scalable** — can become a real product with a community                             |
| 6  | **Angular + Spring Boot** is an enterprise stack, valued in the market                |
| 7  | **Mixes algorithms + engineering** — demonstrates broad knowledge                     |
| 8  | **16 points mapped** — safety margin above the minimum of 14                          |

---

## 9. Cons

| #  | Disadvantage                                   | Mitigation                                                         |
|----|------------------------------------------------|--------------------------------------------------------------------|
| 1  | **High complexity** — sandbox + judge is heavy   | Simplified MVP: input/output tests, no Big-O analysis             |
| 2  | **Heavy infrastructure** — Docker runner, Redis, queues | Use docker-compose, everything local, no load balancing      |
| 3  | **Cheating** — code copying, AI solving          | MVP: ignore anti-cheat, focus on core functionality               |
| 4  | **Development time** — intensive workload     | Adoption of Scrum methodology and efficient team allocation |
| 5  | **Challenge bank** — requires content           | Initialization with 20-30 fixed challenges, manually categorized |
| 6  | **Code language** — support multiple?            | MVP: support only 1-2 languages (Python + JavaScript)              |
| 7  | **Docker-in-Docker** — containers inside compose | Utilization of Docker socket mounting or a separate runner service |

---

## 10. MVP — Minimum Viable Product (Recommended)

The MVP focuses on the following essentials:

### Included in MVP
- [x] Login/registration with JWT
- [x] Profile with avatar and basic statistics
- [x] Friends system and online status
- [x] Basic chat functionality
- [x] Elo-based matchmaking queue
- [x] Duel interface with Monaco editor and timer
- [x] Code execution in a Docker container (Python only)
- [x] Evaluation: correctness and submission time
- [x] Ranking ladder (leaderboard)
- [x] Basic notifications (duel result)
- [x] 20 initial challenges (Easy/Medium/Hard)
- [x] WebSocket for real-time duel

### Outside MVP (phase 2)
- [ ] Spectator mode
- [ ] Ghost Battles / AI Opponent
- [ ] Tournaments with brackets
- [ ] Performance analysis (Big-O, memory)
- [ ] Code quality analysis
- [ ] Multiple language support
- [ ] Anti-cheat
- [ ] Custom challenges created by participants
- [ ] OAuth with GitHub/Google

---

## 11. Suggested Work Division (Team of 4-5)

| Role            | Responsibility                                                        |
|-----------------|-----------------------------------------------------------------------|
| **PO**          | Product vision, challenge backlog, feature validation                 |
| **Tech Lead**   | Architecture, Docker sandbox, code review, integration                |
| **Backend**     | Spring Boot API, matchmaking, judge service, WebSocket                |
| **Frontend**    | Angular UI, Monaco Editor, lobby, arena, ranking, profile             |
| **Full Stack**  | Chat, notifications, user management, tests, docker-compose deploy   |

---

## 12. Folder Structure (Initial Suggestion)

```
code-arena/
├── docker-compose.yml
├── .env.example
├── README.md
│
├── frontend/                    # Angular
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/            # Auth, guards, interceptors
│   │   │   ├── shared/          # Reusable components
│   │   │   ├── features/
│   │   │   │   ├── auth/        # Login, registration
│   │   │   │   ├── lobby/       # Matchmaking
│   │   │   │   ├── arena/       # Editor + duel
│   │   │   │   ├── profile/     # Profile + stats
│   │   │   │   ├── ranking/     # Leaderboard
│   │   │   │   ├── chat/        # Messages
│   │   │   │   └── challenges/  # Challenge list
│   │   │   └── app.module.ts
│   │   └── environments/
│   └── Dockerfile
│
├── backend/                     # Spring Boot
│   ├── src/main/java/com/codearena/
│   │   ├── config/              # Security, WebSocket, Docker config
│   │   ├── auth/                # JWT, AuthController
│   │   ├── user/                # UserController, UserService
│   │   ├── matchmaking/         # MatchmakingService, Queue
│   │   ├── duel/                # DuelController, DuelService
│   │   ├── judge/               # JudgeService, DockerRunner
│   │   ├── challenge/           # ChallengeController, ChallengeService
│   │   ├── ranking/             # RankingService, EloCalculator
│   │   ├── chat/                # ChatController, ChatService
│   │   └── notification/        # NotificationService
│   ├── src/main/resources/
│   │   └── application.yml
│   └── Dockerfile
│
├── sandbox/                     # Docker image for code execution
│   ├── Dockerfile               # Isolated Python runtime
│   └── runner.py                # Script that executes and collects metrics
│
└── database/
    └── init.sql                 # Initial schema
```

---

## 13. Standout Features (to differentiate)

| Feature              | Description                                                      |
|----------------------|------------------------------------------------------------------|
| **Spectator Mode**   | Watch duels live with a security delay                           |
| **Ghost Battles**    | Fight against replays of top players' solutions                  |
| **Code DNA**         | Coding style profile (verbose, functional, recursive, etc.)      |
| **Speed Mode**       | Blitz coding: 2-minute problems                                  |
| **Async Duel**       | Chess-style submission: winners are simulated by the system     |
| **Replay**           | Watch recordings of past duels keystroke by keystroke             |

---

## 14. Open Decisions

These questions should be discussed with the team before starting:

1. **Visual**: Cyberpunk/gamer UI or clean enterprise?
2. **Judge**: Real Docker (recommended) or simulated with static tests?
3. **Ranking**: Global or by classes/groups?
4. **Languages**: Python only in MVP or start with JS as well?

---

*Document generated as a planning foundation for ft_transcendence — Code Arena.*
