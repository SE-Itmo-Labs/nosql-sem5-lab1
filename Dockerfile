FROM eclipse-temurin:25-jre-alpine

WORKDIR /nosql-sem5-lab1

COPY build/libs/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
