FROM maven:3.9.16-eclipse-temurin-26 AS builder
WORKDIR /app
COPY pom.xml .

RUN mvn dependency:go-offline -B
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:26-jdk AS runner
WORKDIR /app
COPY --from=builder /app/target/order-service-0.0.1-SNAPSHOT.jar ./app.jar

# Expõe a porta HTTP (REST)
EXPOSE 4008

ENTRYPOINT ["java", "-jar", "app.jar"]
