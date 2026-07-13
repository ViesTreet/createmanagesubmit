FROM eclipse-temurin:17-jdk-jammy

# Instalar LibreOffice dentro del contenedor
RUN apt-get update && apt-get install -y \
    libreoffice \
    libreoffice-java-common \
    && rm -rf /var/lib/apt/lists/*

# Crear la carpeta estática que requiere tu variable ST_FOLDER
RUN mkdir -p /app/static//app/static/plantillas

WORKDIR /app

# Copiar tu jar. Asegúrate de que el nombre coincida con el que generó Maven
COPY target/createmanagesubmit-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]