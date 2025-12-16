FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/search-credit-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8189
ENTRYPOINT ["java","-jar","app.jar"]
