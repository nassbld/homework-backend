# --- Étape 1 : Build (Compilation) ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# On lance la compilation DANS Docker (Render va le faire)
RUN mvn clean package -DskipTests

# --- Étape 2 : Run (Exécution) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# On récupère le .jar créé à l'étape 1
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
