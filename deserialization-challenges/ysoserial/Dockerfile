FROM maven:3.8.6-eclipse-temurin-8 AS build

WORKDIR /build
COPY site/pom.xml ./pom.xml
RUN mvn -q -DskipTests dependency:go-offline
COPY site/src ./src
RUN mvn -q -DskipTests package

FROM openjdk:8u111-jre

RUN rm -f /etc/apt/sources.list.d/*.list \
    && printf '%s\n' \
      'Acquire::Check-Valid-Until "false";' \
      'Acquire::AllowInsecureRepositories "true";' \
      'Acquire::AllowDowngradeToInsecureRepositories "true";' \
      > /etc/apt/apt.conf.d/99archive \
    && printf '%s\n' \
      'deb [trusted=yes] http://archive.debian.org/debian jessie main' \
      'deb [trusted=yes] http://archive.debian.org/debian-security jessie/updates main' \
      > /etc/apt/sources.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends bash curl netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

RUN for security_file in \
      /opt/java/openjdk/lib/security/java.security \
      /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security; do \
      if [ -f "$security_file" ]; then \
        printf '%s\n' \
          'jdk.serialFilter=*' \
          'sun.rmi.transport.dgcFilter=*' \
          'sun.rmi.registry.registryFilter=*' \
          >> "$security_file"; \
      fi; \
    done

WORKDIR /opt
RUN mkdir -p /var/www/static

COPY --from=build /build/target/java-deser-challenge-1.0.0.jar /opt/app.jar
COPY site/flag.txt /opt/flag.txt
COPY site/flag.txt /opt/flag_fantoccio.txt

EXPOSE 8080
CMD ["java", "-jar", "/opt/app.jar"]
