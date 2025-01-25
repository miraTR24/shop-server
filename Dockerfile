# Étape 1 : Utiliser une image Maven pour construire le projet
FROM maven:3.9.4-eclipse-temurin-21 as build

WORKDIR /app

# Copier les fichiers de configuration et le code source
COPY pom.xml ./
COPY src ./src

# Construire l'application avec Maven
RUN mvn clean package -DskipTests

# Étape 2 : Utiliser une image Java pour exécuter l'application
FROM openjdk:21

WORKDIR /app

# Copier le JAR généré dans l'image finale
COPY --from=build /app/target/*.jar shop-app.jar

# Exposer le port 8080
EXPOSE 8080

# Démarrer l'application
ENTRYPOINT ["java","-jar","shop-app.jar"]