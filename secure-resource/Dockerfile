FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/secure-resource-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9100

ENTRYPOINT ["java", "-jar", "app.jar"]
