FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY src ./src
RUN javac -d out src/Main.java

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "out", "Main"]
