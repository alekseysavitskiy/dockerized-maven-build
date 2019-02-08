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
docker build -f docker/Dockerfile -t marius/dockerized-maven-jdk11 .
```

Once the image is built, we can proceed to build the source code
by referencing the volumes:
- project
- repository (m2)


```
docker run -it \
           --rm  \
           -v $(pwd):/project \
           -v $(echo "$HOME/.m2/repository"):/repository \
           marius/dockerized-maven-jdk11 mvn clean compile
```


When executing the command mentioned above, the source files of the project
will be successfully compiled.

```
[INFO] --- maven-compiler-plugin:3.8.0:compile (default-compile) @ dockerized-maven-build ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /project/target/classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.250 s
```


In case of the showcased project however (because a Postgres docker container is spawned for 
test purposes) this approach needs still to be tweaked in order to point to the container 
building the source code how to communicated with the docker daemon for 
spawning a new container (Postgres). 

https://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/

```
This looks like Docker-in-Docker, feels like Docker-in-Docker, but it's not Docker-in-Docker: 
when this container will create more containers, those containers will be created in the 
top-level Docker. 

You will not experience nesting side effects, and the build cache will be 
shared across multiple invocations.
```


After adding the UNIX docker socket volume and the `DOCKER_HOST` variable (needed
in the initialization of the testcontainers library when running from within a container)
the project builds successfully

```
docker run -it \
           --rm  \
           -e DOCKER_HOST=unix:///var/run/docker.sock \
           -v /var/run/docker.sock:/var/run/docker.sock \           
           -v $(pwd):/project \
           -v $(echo "$HOME/.m2/repository"):/repository \
           marius/dockerized-maven-jdk11 mvn clean compile
```




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
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.596 s - in eu.docker.postgres.PostgresContainerTest

```

However, there is a drawback - the `target` folder belongs to the `root:root` user:

```
➜  dockerized-maven-build git:(master) ✗ ls -la
...
-rw-r--r--  1 marius marius 5211 Feb  8 13:38 readme.md
drwxr-xr-x  4 marius marius 4096 Feb  8 13:09 src
drwxr-xr-x  8 root   root   4096 Feb  8 13:36 target

```

Doing things in this manner has the drawback that the `target` file is not writable anymore
by the logged in user `marius` when doing from the command line build (or when changing 
in the IDE a file and compile afterwards the project).

In order to solve this problem, the docker container needs to be executed as a non root user.

https://medium.com/redbubble/running-a-docker-container-as-a-non-root-user-7d2e00f8ee15

It must be noted here that in order to be able to spawn the postgres testcontainers instance, 
the user group specified must be the same as the `docker` user group from the host machine.

Retrieve the logged-in user and the docker group to which it is assigned:
```
--user $(id -u):$(cut -d: -f3 < <(getent group docker)) \
```

The modified `docker run` command looks now like this:

```
docker run -it \
           --rm  \
           --user $(id -u):$(cut -d: -f3 < <(getent group docker)) \           
           -e DOCKER_HOST=unix:///var/run/docker.sock \
           -v /var/run/docker.sock:/var/run/docker.sock \           
           -v $(pwd):/project \
           -v $(echo "$HOME/.m2/repository"):/repository \
           marius/dockerized-maven-jdk11 mvn clean compile
```


As you can see from the listing below, the `target` folder belongs now to
the user `marius` (and its `docker` group).

```
➜  dockerized-maven-build git:(master) ✗ ls -la
...
drwxr-xr-x  4 marius marius 4096 Feb  8 13:09  src
drwxr-xr-x  8 marius docker 4096 Feb  8 13:47  target
```

By doing this series of steps in your project, you can have a completely dockerized maven build
with the advantage that it can spawn other containers for test purposes.

Obviously this is a proof of concept and there are very likely some other limitations when working
with a more complex project, but this could be good start when thinking about containerized builds.

Feedback is welcome.