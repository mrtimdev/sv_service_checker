# Use a Java 17 JDK base image (adjust version if needed)
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /src

# Copy Maven wrapper and pom files first (for caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src src

# Build the project (with DevTools)
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "timdev-0.0.1-SNAPSHOT.jar"]
