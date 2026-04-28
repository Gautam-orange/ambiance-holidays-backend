FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache argon2-libs
WORKDIR /app
COPY target/ambiance-holidays-api-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
