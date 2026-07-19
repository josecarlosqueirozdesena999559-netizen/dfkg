# Build stage
FROM openjdk:17-slim AS build
WORKDIR /home/app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew installDist --no-daemon

# Run stage
FROM openjdk:17-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/app/build/install/decisoes-backend /app/decisoes-backend
WORKDIR /app/decisoes-backend
ENTRYPOINT ["bin/decisoes-backend"]
