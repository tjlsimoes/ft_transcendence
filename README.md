# Code Arena - ft_transcendence

PvP Code Duel platform com sistema de ranking Elo.

## 🚀 Como Correr o Projeto

### 1. Backend (Spring Boot)

**Pré-requisitos:**
- Java 21
- Maven
- Docker & Docker Compose

**Passos:**

```bash
# 1. Iniciar PostgreSQL e Redis
docker-compose up -d

# 2. Navegar para o backend
cd backend/code-arena-backend

# 3. Rodar o projeto
./mvnw spring-boot:run
```

Backend disponível em: **http://localhost:8080**

Testa: **http://localhost:8080/api/health**

---

### 2. Frontend (Angular)

**Pré-requisitos:**
- Node.js 18+
- npm

**Passos:**

```bash
# 1. Navegar para o frontend
cd frontend

# 2. Instalar dependências (só primeira vez)
npm install

# 3. Iniciar o servidor
npm start
```

Frontend disponível em: **http://localhost:4200**

---

## 📁 Estrutura do Projeto

```
ft_transcendence/
├── backend/code-arena-backend/    # Spring Boot API
│   └── src/main/java/com/codearena/code_arena_backend/
│       ├── config/                # Configurações (CORS, Security, WebSocket)
│       ├── auth/                  # Autenticação JWT
│       ├── user/                  # Gestão de utilizadores
│       ├── matchmaking/           # Sistema de matchmaking
│       ├── duel/                  # Lógica dos duelos
│       ├── judge/                 # Execução de código
│       ├── challenge/             # Gestão de desafios
│       ├── ranking/               # Sistema ELO
│       ├── chat/                  # Chat entre users
│       └── notification/          # Notificações
│
├── frontend/                      # Angular SPA
├── sandbox/                       # Docker sandbox para código
└── docker-compose.yml            # PostgreSQL + Redis
```

---

## 🎯 Próximos Passos (Backend)

### FASE 1: Autenticação ✅ (Setup básico feito)
- [x] User entity criada
- [x] UserRepository criado
- [x] CORS configurado
- [x] Security temporária
- [ ] **Próximo:** Implementar JWT e AuthService

### FASE 2: Autenticação JWT (FAZER AGORA)
1. Criar `JwtUtil` em `config/`
2. Criar DTOs: `LoginRequest`, `RegisterRequest`, `AuthResponse`
3. Criar `AuthService` com login/register
4. Criar `AuthController` com endpoints `/api/auth/login` e `/api/auth/register`
5. Atualizar `SecurityConfig` para usar JWT

### FASE 3: WebSocket & Matchmaking
1. Configurar WebSocket
2. Implementar fila de matchmaking com Redis
3. Criar lógica de encontrar oponente por ELO

### FASE 4: Challenge Service
1. Criar entidade `Challenge`
2. Popular base de dados com desafios iniciais
3. Endpoint para listar desafios por dificuldade

### FASE 5: Duel Service
1. Criar entidade `Duel`
2. Lógica de iniciar duelo
3. Timer de duelo
4. Submissão de código

### FASE 6: Judge Service (Docker Sandbox)
1. Criar Docker image para execução de código
2. Implementar executor de código Python
3. Avaliar correção e performance

### FASE 7: Ranking & Stats
1. Implementar cálculo de ELO
2. Atualizar stats dos users
3. Endpoint de leaderboard

### FASE 8: Chat & Friends
1. Sistema de amizades
2. Chat entre users via WebSocket

---

## 🗄️ Comandos Úteis

```bash
# Ver logs do PostgreSQL
docker logs codearena-postgres

# Ver logs do Redis
docker logs codearena-redis

# Parar tudo
docker-compose down

# Parar e remover volumes (reset da BD)
docker-compose down -v

# Backend: compilar sem rodar
cd backend/code-arena-backend && ./mvnw clean install

# Backend: rodar testes
./mvnw test
```

---

## 📚 Documentação do Projeto

Ver [project-code-arena.md](project-code-arena.md) para detalhes completos do projeto.
