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
- `sbx-server/.../database/DBService.java` — All SQL queries; add new DB methods here
- `sbx-server/.../database/DbUtils.java` — Converts `MachineRow` → `Machine` REST type
- `sbx-server/.../database/DatabaseServer.java` — H2 lifecycle
- `sbx-server/src/main/resources/schema.sql` — Table definitions
- `sbx-server/src/main/resources/sql_seed.sql` — Sample data (30 VMs, seeds, ports, storage)

### REST Handler Layout

Handlers live under `sbx-server/.../web/rest/` organized by domain:

| Package | Handlers |
|---------|---------|
| `web/rest/vm/` | `CreateVm`, `GetVm`, `StartVm`, `UpdateVm`, `DeleteVm`, `PinVm` |
| `web/rest/network/` | `CreateNatNetwork`, `GetNatNetwork`, `CreatePortForwardRule`, `UpdatePortForwardRule` |
| `web/rest/storage/` | `CreateStorageUnits`, `GetStorageUnits` |
| `web/rest/ux/` | `ListMachines`, `ListSeeds`, `LocalResources`, `LocalVmResources`, `SbxSettings` |
| `web/rest/observability/` | `GetOperationState` |

### REST API Routes

| Method | Path | Handler | Actor call |
|--------|------|---------|-----------|
| POST | `/api/machine` | `CreateVm` | `CloneMachineRequest`, `CreateNatNetworkRequest` |
| GET | `/api/machine/{uuid}` | `GetVm` | — |
| POST | `/api/machine/start` | `StartVm` | `LaunchMachineRequest` |
| PUT | `/api/machine` | `UpdateVm` | — |
| PUT | `/api/machine/pin` | `PinVm` | — |
| DELETE | `/api/machine/{uuid}` | `DeleteVm` | `CleanUpVMRequest`, `RemoveNatNetworkRequest` |
| POST | `/api/nat-network` | `CreateNatNetwork` | `CreateNatNetworkRequest` |
| GET | `/api/nat-network` | `GetNatNetwork` | — |
| POST | `/api/port-forward-rule` | `CreatePortForwardRule` | `AddNATNetworkPortForwardRuleRequest` |
| PUT | `/api/port-forward-rule` | `UpdatePortForwardRule` | `RmNATNetworkPortForwardRuleRequest`, `AddNATNetworkPortForwardRuleRequest` |
| POST | `/api/storage-unit` | `CreateStorageUnits` | `CreateSharedStorageRequest`, `AddSharedStorageToMachineRequest` |
| GET | `/api/storage-unit` | `GetStorageUnits` | — |
| GET | `/api/machines/list` | `ListMachines` | — |
| GET | `/api/vm-seeds/list` | `ListSeeds` | — |
| GET | `/api/local-resources` | `LocalResources` | — |
| GET | `/api/local-vm-resources` | `LocalVmResources` | — |
| GET | `/api/sbx-settings` | `SbxSettings` | — |
| POST | `/api/operation/state` | `GetOperationState` | — |
| GET | `/api/ping` | inline | — |

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
- Each REST endpoint has its own handler class implementing `io.javalin.http.Handler`
- Request/response DTOs live in `sbx-common` under `dev.donhk.rest.*` (organized by domain: `operations/vm/`, `network/`, `storage/`, `types/`)
- Database row POJOs live in `dev.donhk.pojos.*` (e.g., `MachineRow`, `VMPortRow`)
- VirtualBox operations go through `VBoxActor` via `Utilities.askSync(vboxActor, request)` — never call `VBoxManager` directly from the REST layer
- All `VBoxMessage` request/response pairs are Java records in `vbox-glue/.../actor/VBoxMessage.java`; each has a corresponding impl class in `actor/impl/` and a `.match()` handler in `VBoxActor.createReceive()`
- `DBService` is the only place with SQL; handlers call it for all DB reads and writes
- `DbUtils.machineRow2Machine(db, row)` is the canonical way to convert a `MachineRow` to a `Machine` REST response
- Handlers that need the actor port pool use `config.sbxServiceLowPort` / `config.sbxServiceHighPort` (default 11200–11500) and call `db.findUsedHostPorts()` to find a free slot
- NAT network names must match `\w+_\w+` pattern (e.g., `mch001_network`) so `DelDanglingNets` can identify them
- Logging: TinyLog via SLF4J facade

## Database Schema Summary

| Table | Purpose |
|-------|---------|
| `virtual_machines` | One row per VM; `id` is the UUID (e.g. `mch-031`) |
| `vm_ports` | Port forward rules; `host_port` comes from the port pool |
| `vm_storage_units` | Attached VDI disks; `name` is the full disk path on the host |
| `vm_seeds` | Snapshot templates used as clone sources; keyed on `(prefix, snapshot_name)` |
| `vms_history` | Append-only log of deleted VMs |
| `resources_table` | Time-series resource metrics (CPU, RAM, storage, network) |
| `instances` | Server hostnames |

## Actor Message Map

All VBoxManager operations are exposed as messages in `VBoxMessage`. The full set:

`GetVBoxVersionRequest` · `CloneMachineRequest` · `LaunchMachineRequest` · `AddSharedDirectoryRequest` · `CreateSharedStorageRequest` · `AddSharedStorageToMachineRequest` · `AddNATNetworkPortForwardRuleRequest` · `AddNATPortForwardRuleRequest` · `RmNATPortForwardRuleRequest` · `RmNATNetworkPortForwardRuleRequest` · `GetPortForwardRulesRequest` · `GetMachineIPv4Request` · `CleanUpVMRequest` · `MachineExistsRequest` · `CreateNatNetworkRequest` · `RemoveNatNetworkRequest` · `ListMachinesRequest` · `DelDanglingNetsRequest`
