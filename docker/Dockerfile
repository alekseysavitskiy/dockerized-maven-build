FROM openjdk:11-jdk

ARG dockerHost

# Install maven
WORKDIR /
ENV MAVEN_VERSION="3.6.0"
RUN curl "https://apache.volia.net/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" --output maven.tar.gz \
    && tar -xzf 'maven.tar.gz' \
    && ln -s apache-maven-${MAVEN_VERSION} maven
ENV PATH="$PATH:/maven/bin"

ENV DOCKER_HOST=tcp://localhost:2376
ENV DOCKER_CERT_PATH=/project/certs
ENV DOCKER_TLS_VERIFY=1
ENV TESTCONTAINERS_RYUK_DISABLED="true"
ENV DOCKER_CONFIG_FILE=/project/config.json

# Override the m2 settings in order to point to the host machine's m2 repo
COPY docker/setup/settings.xml /settings.xml

RUN rm -f /maven/conf/settings.xml >/dev/null
RUN cp /settings.xml /maven/conf/settings.xml


WORKDIR /project
COPY pom.xml ./

RUN mvn -B de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

# Build all the things
COPY . ./


#RUN mvn clean deploy
# for the sake of keeping things simple, we'll only perform the tests and not the deployment
# of the artifact to the maven central repository.
RUN mvn clean test