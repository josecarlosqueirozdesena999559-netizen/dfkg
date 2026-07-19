# Build stage
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist --no-daemon

# Run stage
FROM openjdk:17-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/install/decisoes-backend /app/decisoes-backend
ENTRYPOINT ["/app/decisoes-backend/bin/decisoes-backend"]
