Using testcontainers in a dockerized maven build
================================================

This simple project is just a showcase on how could the entire build 
process for a project can be performed in a dockerized environment.

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
