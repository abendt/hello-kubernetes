FROM anapsix/alpine-java:8_server-jre
MAINTAINER abendt

EXPOSE 8080

ENTRYPOINT java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -cp /app/ org.springframework.boot.loader.JarLauncher

HEALTHCHECK CMD /opt/bin/health.sh

ADD src/main/scripts/health.sh /opt/bin/

RUN apk --no-cache add curl

# add Spring Boot launcher classes
COPY build/docker/org/ /app/org/

# add Logback configuration
#COPY ./build/docker/BOOT-INF/classes/logback.xml /app/BOOT-INF/classes/

# runtime libraries
COPY ./build/docker/BOOT-INF/lib/ /app/BOOT-INF/lib/

# application configuration
#COPY ./build/docker/BOOT-INF/classes/application.properties /app/BOOT-INF/classes/

# Spring Boot banner (updated when version changes)
#COPY ./build/docker/BOOT-INF/classes/banner.txt /app/BOOT-INF/classes/

# application classes
COPY ./build/docker/BOOT-INF/classes/hellorest /app/BOOT-INF/classes/hellorest

# manifest file always recreated by Spring Boot
COPY ./build/docker/META-INF /app/META-INF/