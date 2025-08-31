# Используем доступный образ Maven для сборки
FROM maven:3.9.11-amazoncorretto-17-al2023 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем только pom.xml сначала (для лучшего кэширования)
COPY pom.xml .

# Скачиваем зависимости (они будут закэшированы)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN mvn clean package -DskipTests

# Используем официальный образ Amazon Corretto для запуска
FROM amazoncorretto:17-al2023

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR-файл из стадии сборки
COPY --from=build /app/target/*.jar app.jar

# Для Amazon Linux 2023 используем альтернативный способ создания пользователя
RUN yum install -y shadow-utils && \
    groupadd -r jirauser && \
    useradd -r -g jirauser jirauser && \
    yum remove -y shadow-utils && \
    yum clean all

RUN chown -R jirauser:jirauser /app
USER jirauser

# Открываем порт, который использует приложение
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]