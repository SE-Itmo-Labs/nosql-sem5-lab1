FROM eclipse-eclipse-temurin:21-jre-apline

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "jar", "app.jar"]