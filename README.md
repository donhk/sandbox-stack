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

This section is WiP

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