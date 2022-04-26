FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8080
ADD /build/libs/spring-arrow-example.jar application.jar
ENTRYPOINT ["java","-jar","-Djava.security.egd=file:/dev/./urandom","application.jar"]
