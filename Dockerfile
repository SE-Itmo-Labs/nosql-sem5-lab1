FROM eclipse-temurin:25-jre-alpine

WORKDIR /nosql-sem5-lab1

COPY build/libs/*.jar nosql-sem5-lab1-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "nosql-sem5-lab1-1.0-SNAPSHOT.jar"]