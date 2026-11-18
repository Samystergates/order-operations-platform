FROM maven:3.9.8-eclipse-temurin-8 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/erp-adapter.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/erp-adapter.jar"]
