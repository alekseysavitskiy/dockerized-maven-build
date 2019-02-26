Using testcontainers in a dockerized maven build
================================================

This simple project is just a showcase on how could the entire build 
process (not only spawning containers for test purposes) for a project 
can be performed in a dockerized environment.

There are though some struggles with `docker` which I've faced 
while making this proof of concept, reason why I thought it would
be beneficial to share my findings with other software engineer
who may be trying to do something similar in their projects.
 

## Introduction

The underlying technologies showcased in this project are:

* [docker](https://github.com/docker) 
* [testcontainers](https://github.com/testcontainers/testcontainers-java) 

By running

```
mvn clean install
```

it can be seen that during the maven `test` phase a 
a postgres container (through the usage of testcontainers library)
is being spawned.


```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running eu.docker.postgres.PostgresContainerTest
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
        ℹ︎ Checking the system...
        ✔ Docker version should be at least 1.6.0
        ✔ Docker environment should have more than 2GB free disk space
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.5 s - in eu.docker.postgres.PostgresContainerTest
```

The above mentioned test spawns two containers:

```
➜  ~ docker ps
CONTAINER ID        IMAGE                               COMMAND                  CREATED             STATUS              PORTS                     NAMES
44786704c442        postgres:9.6.8                      "docker-entrypoint.s…"   2 seconds ago       Up 1 second         0.0.0.0:32799->5432/tcp   boring_lamarr
644c74ed0cec        quay.io/testcontainers/ryuk:0.2.2   "/app"                   2 seconds ago       Up 1 second         0.0.0.0:32798->8080/tcp   testcontainers-ryuk-00894707-a42f-4ce3-a064-e9d8a6ceea0c
```


## Dockerized builds

Probably a common question when looking at this project would be why bother
with a dockerized build.
A situation very fashionable lately is the transition to new Open JDK java version 
every version. Having a dockerized build allows a much less painful upgrade to the 
new java version.

The why question for the dockerized maven builds is answered in detail
on the following website:

https://dzone.com/articles/maven-build-local-project-with-docker-why

TLDR; build an image with the dependencies for the project (java, maven, etc.)

Building the image is done by executing the following statement:

```
docker build -f docker/Dockerfile -t marius/dockerized-maven-jdk11 dockerHost=tcp://my.docker.host:2375 .
```


The `dockerHost` build argument referenced in the command above is located on another machine in my scenario (not the same machine as the one where the docker build is performed). 

When building the image, I've struggled with enabling ryuk, so in a first phase I've disabled it through

```
ENV TESTCONTAINERS_RYUK_DISABLED="true"
```


By doing this, the building of the image succeeds.


```
➜  dockerized-maven-build git:(master) ✗ docker build -f docker/Dockerfile -t marius/dockerized-maven-jdk11 --build-arg dockerHost=tcp://my.docker.host:2375 .

....

[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.3 s - in eu.docker.postgres.PostgresContainerTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.703 s
[INFO] Finished at: 2019-02-26T06:26:48Z
[INFO] ------------------------------------------------------------------------
Removing intermediate container 862135fb53e6
 ---> 751ca9bfb34f
Successfully built 751ca9bfb34f
Successfully tagged marius/dockerized-maven-jdk11:latest

```


When using `ryuk` for test purposes, by commenting the `Dockerfile` line

```
#ENV TESTCONTAINERS_RYUK_DISABLED="true"
``` 

the build fails with the following exception:

```
...

2019-02-25 16:06:46 WARN  ResourceReaper:137 - Can not connect to Ryuk at my.docker.host:32866
java.net.ConnectException: Connection refused (Connection refused)
	at java.base/java.net.PlainSocketImpl.socketConnect(Native Method)
	at java.base/java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:399)
	at java.base/java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:242)
	at java.base/java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:224)
	at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:403)
	at java.base/java.net.Socket.connect(Socket.java:591)
	at java.base/java.net.Socket.connect(Socket.java:540)
	at java.base/java.net.Socket.<init>(Socket.java:436)
	at java.base/java.net.Socket.<init>(Socket.java:213)
	at org.testcontainers.utility.ResourceReaper.lambda$start$1(ResourceReaper.java:112)
	at java.base/java.lang.Thread.run(Thread.java:834)

	
...
...
	
[ERROR] Errors: 
[ERROR]   PostgresContainerTest » ExceptionInInitializer
[INFO] 
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  35.332 s
[INFO] Finished at: 2019-02-26T06:30:36Z
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M3:test (default-test) on project dockerized-maven-build: There are test failures.
[ERROR] 
[ERROR] Please refer to /project/target/surefire-reports for the individual test results.
[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
The command '/bin/sh -c mvn clean test' returned a non-zero code: 1

```

This showcase is meant to share more light on the comments i've made here:

https://github.com/testcontainers/testcontainers-java/pull/1181

