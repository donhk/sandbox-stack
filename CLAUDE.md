# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sandbox-Stack is a VirtualBox-based VM management platform for automated cluster provisioning and testing. It exposes a REST API backed by Akka actors for async VirtualBox operations, an embedded H2 database, and a React dashboard served as static assets from the backend.

## Module Structure

Gradle monorepo with these modules (defined in `settings.gradle`):

- **sbx-server** — Javalin REST API, H2 database, main service entry point
- **sbx-common** — Shared DTOs (`dev.donhk.rest.*`) and POJOs (`dev.donhk.pojos.*`)
- **vbox-glue** — Akka actors wrapping the VirtualBox native SDK
- **sbx-client** — Placeholder client (mostly empty)
- **sbx-front-end** — React/Vite dashboard (built output deployed into `sbx-server/src/main/resources/public/`)

Dependency chain: `sbx-server` → `sbx-common` + `vbox-glue` → `sbx-common`

## Build & Run Commands

### Backend (Java/Gradle)

```bash
./gradlew build                    # Build all modules
./gradlew assemble                 # Build without running tests
./gradlew clean                    # Clean build artifacts
./gradlew :sbx-server:run          # Run server (production)
./gradlew :sbx-server:runDev       # Run server with DB reset + seed data
./gradlew test                     # Run all tests
./gradlew :sbx-server:test         # Run server tests only
./gradlew :vbox-glue:test          # Run vbox-glue tests only
```

Tests use JUnit 5. The root `build.gradle` passes `-XX:+EnableDynamicAgentLoading` to the JVM for Mockito compatibility.

### Frontend (React/Vite)

```bash
cd sbx-front-end
npm install
npm start          # Dev server on port 3000
npm run build      # Production build (copies output to sbx-server/src/main/resources/public/)
npm run lint       # ESLint + Prettier
npm run serve      # Preview production build
```

## Architecture

```
React Frontend (port 3000 dev / embedded in prod)
        │ REST via Axios
        ▼
Javalin HTTP (port 8008)
   ├── REST Handlers (one class per endpoint)
   ├── DBService → HikariCP → H2 (TCP port 9094, web console 8082)
   └── VBoxActor (Akka) → vbox-glue → VirtualBox native API
```

### Key Backend Files

- `sbx-server/.../boot/ServerMain.java` — CLI entry point (PicoCLI)
- `sbx-server/.../server/SandboxerApp.java` — Wires up all services
- `sbx-server/.../server/HttpService.java` — All REST route definitions
- `sbx-server/.../database/DatabaseServer.java` — H2 lifecycle
- `sbx-server/src/main/resources/schema.sql` — Table definitions
- `sbx-server/src/main/resources/sql_seed.sql` — Sample data

### Key Frontend Files

- `sbx-front-end/src/App.js` — Router setup
- `sbx-front-end/src/routes.js` — Route definitions
- `sbx-front-end/src/config.js` — Loads `/config.json` at runtime
- `sbx-front-end/src/store.js` — Redux store (theme, global state)

## Server CLI Options

The server is configured entirely via command-line args (no config files):

| Flag | Default | Purpose |
|------|---------|---------|
| `-s` | 8008 | REST API port |
| `-d` | sandbox | DB name |
| `-u` / `-p` | dbmaster / welcome | DB credentials |
| `-w` | 8082 | H2 web console port |
| `-t` | 9094 | H2 TCP port |
| `-l` / `-h` | 11200 / 11500 | Port pool range for VMs |
| `-r` | false | Reset DB schema on startup |
| `-ss` | false | Seed sample data on startup |

`runDev` Gradle task passes `-r -ss` automatically.

## Code Conventions

- All Java packages under `dev.donhk.*`
- Each REST endpoint has its own handler class (e.g., `GetVm`, `ListMachines`, `DeleteMachine`)
- Request/response DTOs live in `sbx-common` under `dev.donhk.rest.*`
- Database row POJOs live in `dev.donhk.pojos.*`
- VirtualBox operations are always async via Akka actor messaging in `vbox-glue`
- Logging: TinyLog via SLF4J facade
