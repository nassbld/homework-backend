# On utilise juste une image Java légère pour l'exécution
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# On copie le fichier .jar que VOUS venez de construire manuellement
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
