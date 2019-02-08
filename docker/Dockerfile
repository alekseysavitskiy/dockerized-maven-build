FROM openjdk:11-jdk
MAINTAINER Marius Grama <mariusneo@gmail.com>

# Install maven
WORKDIR /
ENV MAVEN_VERSION="3.6.0"
RUN curl "https://www-eu.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" --output maven.tar.gz \
    && tar -xzf 'maven.tar.gz' \
    && ln -s apache-maven-${MAVEN_VERSION} maven
ENV PATH="$PATH:/maven/bin"

# Override the m2 settings in order to point to the host machine's m2 repo
COPY docker/setup/settings.xml /settings.xml

# Add an entry point for the project
ADD docker/setup/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

RUN rm -f /maven/conf/settings.xml >/dev/null
RUN cp /settings.xml /maven/conf/settings.xml


WORKDIR /project

# Configure a `repository` volume for m2 repo and a `project` volume for the project
# to be built in the dockerized environment.
VOLUME ["/project", "/repository"]
ENTRYPOINT ["/entrypoint.sh"]