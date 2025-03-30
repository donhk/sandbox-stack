# Sandbox-Stack 🚀

Sandboxer server and client

```bash
./gradlew spotlessApply
```

```bash
./gradlew spotlessCheck
```

# Server 🖥️

To compile this source code you need Java >= 1.8 installed plus gradle 4.6

### Gradle tasks for compilation ###

* gradlew clean # removes old data
* gradlew compileJava # compiles source code
* gradlew jar # creates a jar of the code
* gradlew generate # creates a tree with all the dependencies and wrappers
* gradlew shipit # like generate but compressed ready for distribution
* gradlew serverRun # execute the code as server
* gradlew clientRun # execute the code as clientConnection

### Start up the server on windows from the IDE ###

Before executing the sandboxer server main in Windows, it is required to download
the it should virtualbox SDK and to copy the `vboxjws.jar` under the `main/libs/`
folder and execute the below command

    vboxwebsrv -H 0.0.0.0 -A null

For UNIX system it is recommended to use the xcom version of the libraries

Once the libraries and the service (for windows) is set the code can be executed from any IDE
and any other java application.

### How to generate the zip server ###

* Execute the following command

  `./gradlew zipServer`

* It will produce a file called `sandboxer-x.x.zip`

### Contribution guidelines ###

* Make sure every machine added as seed has enough network adapter enabled
* The seed VM must have only one disk connected, no multiple disks are allowed

# Client 🎮

To compile this source code you need Java >= 1.8 installed plus gradle 4.6

### How to generate the libs for distribution ###

```
./gradlew jar
```

will generate the compiled jars under `build/libs`

### How to execute the sandboxer client from an IDE ###

* The Sandboxer server must be up and waiting for connections
* There are different tests in the test directory, they can be executed directly as they
  run with junit but in order for them to run some metadata must be passed through the
  IDE configuration options

As an additional step, it might be required to select the class for the test to run
in the configuration options

```
VMOptions -DserverAdd=http://127.0.0.1:11500:11502 -DvmName=lubuntuc1
```

where the direction is the ip where the server is running and the vmName the seed that
will be cloned.

* `PlainVM` will start a simple VM
* `NetworkTest1` will start two VMs connected through NAT Network