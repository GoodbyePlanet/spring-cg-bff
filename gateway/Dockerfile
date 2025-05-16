FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar

RUN apk add --no-cache curl

EXPOSE 8081

COPY wait-for-auth.sh /wait-for-auth.sh

RUN chmod +x /wait-for-auth.sh

CMD ["/wait-for-auth.sh", "java", "-jar", "app.jar"]

