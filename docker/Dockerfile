FROM openjdk:11-jdk

ARG dockerHost

# Install maven
WORKDIR /
ENV MAVEN_VERSION="3.6.3"
RUN curl "https://apache.volia.net/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" --output maven.tar.gz \
    && tar -xzf 'maven.tar.gz' \
    && ln -s apache-maven-${MAVEN_VERSION} maven
RUN curl -L "https://github.com/docker/compose/releases/download/1.26.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
    && chmod +x  /usr/local/bin/docker-compose

ENV PATH="$PATH:/maven/bin"

ENV DOCKER_HOST=tcp://localhost:2376
ENV DOCKER_CERT_PATH=/project/certs
ENV DOCKER_TLS_VERIFY=1
ENV TESTCONTAINERS_RYUK_DISABLED="false"
ENV DOCKER_CONFIG_FILE=/project/config.json


WORKDIR /project
COPY . ./
RUN mvn clean test