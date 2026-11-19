# ==========================================
# 1. Этап сборки (Maven + JDK 21)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Кэшируем зависимости Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# 2. Этап исполнения (легковесный JRE 21 Alpine)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Установка правильной кодировки UTF-8
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

# Копируем готовый автономный fat-jar со всеми зависимостями
COPY --from=builder /workspace/target/pksmatches-console.jar /app/app.jar

# Запуск интерактивной консоли Java
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8", "-jar", "/app/app.jar"]
