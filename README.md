# Sandbox-Stack 🚀

The sandbox stack is a framework aimed to facilitate the creation of testing pipelines
using virtualbox as a virtualization layer and automate the creation of complex cluster
topologies to make it easier to run and test platform software.

The sandboxer works in a client-service architecture.

The server is in charge of dispatching vm machines requests, these requests
clone pre-existing VMs snapshots and allow its configuration on the fly.

The client is a thin-rest-based-client that can execute and commands in the machine,
install software and configure the machine to connect to other VMs using virtual networks.

## 🧭 Prod-Deployment

```bash
# Install the latest VirtualBox & VirtualBox-Extension Pack
/usr/bin/virtualbox -h       
#Oracle VirtualBox Manager v7.2.6
#Copyright (C) 2005-2026 Oracle and/or its affiliates

# Then update the dependencies
scripts/install.sh 
```

```bash
curl -O donhk/sandbox-stack/releases/xxx/file.tar.xz
tar -xf file.tar.xz
bash sbx-server/start.sh
```

Import the java client in your code & run it


## Development

### Execution

#### 👾 Start Back-End
```bash
./gradlew build
./gradlew :sbx-server:runDev
```

#### 🥞 Start Front-End
```bash
cd sbx-front-end
npm run start
```

## 🔥 Build

### Compile Sandbox Server
```bash
./gradlew build
./gradlew :sbx-server:run
```

### Compile Front-End
```bash
cd sbx-front-end
npm install
npm start
npm run build
```

### Compile Server
```bash
./gradlew build
```

## ⚙️ Server Configuration

The server is configured entirely via command-line flags. All options have sensible defaults for local development.

| Flag | Default | Description |
|------|---------|-------------|
| `-s` | `8008` | REST API port |
| `-d` | `sandbox` | H2 database name |
| `-u` / `-p` | `dbmaster` / `welcome` | DB credentials |
| `-w` | `8082` | H2 web console port |
| `-t` | `9094` | H2 TCP port |
| `-l` / `-h` | `11200` / `11500` | Host port pool range for VM port-forwarding rules |
| `-r` | `false` | Reset DB schema on startup |
| `-ss` | `false` | Seed sample data on startup |
| `-o` | `http://localhost:3000` | Allowed CORS origins (comma-separated) |

### CORS origins

By default only `http://localhost:3000` is allowed. Pass `-o` to add or replace origins:

```bash
# Local dev — already allows localhost:3000 and pop-os.tail9437a0.ts.net:3000
./gradlew :sbx-server:runDev

# Production: only the Tailscale node
./gradlew :sbx-server:run --args="-o http://pop-os.tail9437a0.ts.net:3000"

# Custom set
./gradlew :sbx-server:run --args="-o http://localhost:3000,http://myhost.example.com:3000"
```