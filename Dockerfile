FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY etps.jar etps.toml /app/
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar etps.jar -c etps.toml"]
