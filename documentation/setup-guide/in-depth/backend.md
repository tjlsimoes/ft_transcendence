# Backend Deep Dive

This document explains the technical details of the Code Arena backend, built with Spring Boot.

## The Maven Wrapper (`.mvn/wrapper`)
The `.mvn/wrapper` directory contains the **Maven Wrapper**. It is provided by default in many Spring Boot projects.
- **Purpose**: It allows you to run Maven commands (using `./mvnw`) without having Maven installed on your machine.
- **Benefit**: It ensures that every developer and the CI/CD environment use the exact same version of Maven, preventing "it works on my machine" issues.

## Dockerfile Breakdown
The `backend/Dockerfile` uses a **multi-stage build** to keep the final image small and secure.

### Stage 1: The Build
- **Maven**: A build automation tool primarily used for Java projects. It manages dependencies (libraries your code needs) and handles the build lifecycle (compiling, testing, packaging).
- **`mvn dependency:go-offline`**: This command downloads all the dependencies defined in your `pom.xml` into the image.
    - *Why separate?* Docker caches layers. By downloading dependencies before copying the source code, Docker can reuse this layer in future builds if the `pom.xml` hasn't changed, making builds much faster.
- **`mvn clean package -DskipTests`**:
    - `clean`: Removes the `target` directory (previous build artifacts).
    - `package`: Compiles the code and packages it into a runnable `.jar` file.
    - `-DskipTests`: Skips running unit tests during the build to save time.

### Stage 2: The Runtime
- **eclipse-temurin**: A high-quality, open-source distribution of the Java Development Kit (JDK) maintained by the Adoptium community. It is widely used and trusted.
- **JRE (Java Runtime Environment)**: A subset of the JDK that contains only the components required to *run* a Java application. It does not include compilers or debuggers, which makes the image significantly smaller and more secure.
- **ENTRYPOINT ["java", "-jar", "app.jar"]**:
    - This defines the command that runs when the container starts.
    - It executes the Java runtime and tells it to run the `app.jar` file. Unlike `CMD`, the `ENTRYPOINT` is the main process of the container and is not easily overridden.

## References
- [Official Maven Documentation](https://maven.apache.org/)
- [Maven Wrapper Guide](https://maven.apache.org/wrapper/)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Eclipse Temurin (OpenJDK) Home](https://adoptium.net/temurin/)
- [Docker Multi-Stage Builds](https://docs.docker.com/build/building/multi-stage/)
