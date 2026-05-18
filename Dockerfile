# 1. Usamos Maven para compilar el código (fase de construcción)
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Usamos Tomcat con Java 21 para ejecutar la web
FROM tomcat:10.1-jdk21-temurin
# Borramos lo que trae Tomcat por defecto para que no estorbe
RUN rm -rf /usr/local/tomcat/webapps/*
# Metemos TU código (tu "DVD") renombrado a ROOT.war
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
# Le damos al botón de PLAY del servidor
CMD ["catalina.sh", "run"]