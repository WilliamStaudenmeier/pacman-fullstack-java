import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final List<ScoreEntry> scores = new ArrayList<>();
    private static final Path scoreFile = Path.of("data", "scores.csv");

    public static void main(String[] args) throws IOException {
        int port = parsePort();
        loadScoresFromDisk();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/scores", new ScoreHandler());
        server.setExecutor(null);

        System.out.println("Pacman backend listening on port " + port);
        server.start();
    }

    private static int parsePort() {
        String raw = System.getenv("PORT");
        if (raw == null || raw.isBlank()) {
            return 8080;
        }

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return 8080;
        }
    }

    private static void loadScoresFromDisk() {
        try {
            Files.createDirectories(scoreFile.getParent());
            if (!Files.exists(scoreFile)) {
                return;
            }

            List<String> lines = Files.readAllLines(scoreFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) {
                    continue;
                }

                String name = parts[0].trim();
                int score;
                try {
                    score = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ex) {
                    continue;
                }

                String timestamp = parts[2].trim();
                scores.add(new ScoreEntry(name, score, timestamp));
            }

            sortAndTrimScores();
        } catch (IOException ex) {
            System.err.println("Could not load score file: " + ex.getMessage());
        }
    }

    private static void persistScores() {
        try {
            Files.createDirectories(scoreFile.getParent());
            List<String> lines = new ArrayList<>();
            synchronized (scores) {
                for (ScoreEntry score : scores) {
                    lines.add(score.name + "," + score.score + "," + score.timestamp);
                }
            }
            Files.write(scoreFile, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("Could not persist score file: " + ex.getMessage());
        }
    }

    private static void sortAndTrimScores() {
        synchronized (scores) {
            scores.sort(Comparator.comparingInt((ScoreEntry s) -> s.score).reversed());
            while (scores.size() > 10) {
                scores.remove(scores.size() - 1);
            }
        }
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendNoContent(exchange);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            sendJson(exchange, 200, "{\"status\":\"ok\"}");
        }
    }

    private static class ScoreHandler implements HttpHandler {
        private static final Pattern NAME_PATTERN = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
        private static final Pattern SCORE_PATTERN = Pattern.compile("\\\"score\\\"\\s*:\\s*(\\d+)");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendNoContent(exchange);
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, scoresAsJson());
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePost(exchange);
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String name = extractName(body);
            int score = extractScore(body);

            if (score < 0) {
                sendJson(exchange, 400, "{\"error\":\"Invalid score\"}");
                return;
            }

            synchronized (scores) {
                scores.add(new ScoreEntry(name, score, Instant.now().toString()));
                sortAndTrimScores();
            }
            persistScores();

            sendJson(exchange, 201, "{\"saved\":true}");
        }

        private String extractName(String body) {
            Matcher matcher = NAME_PATTERN.matcher(body);
            if (!matcher.find()) {
                return "PLAYER";
            }

            String name = matcher.group(1).trim();
            if (name.isEmpty()) {
                return "PLAYER";
            }

            if (name.length() > 20) {
                return name.substring(0, 20);
            }

            return name;
        }

        private int extractScore(String body) {
            Matcher matcher = SCORE_PATTERN.matcher(body);
            if (!matcher.find()) {
                return -1;
            }

            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ex) {
                return -1;
            }
        }

        private String scoresAsJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            synchronized (scores) {
                for (int i = 0; i < scores.size(); i++) {
                    ScoreEntry score = scores.get(i);
                    sb.append("{\"")
                      .append("name\":\"")
                      .append(escapeJson(score.name))
                      .append("\",\"score\":")
                      .append(score.score)
                      .append(",\"timestamp\":\"")
                      .append(escapeJson(score.timestamp))
                      .append("\"}");
                    if (i < scores.size() - 1) {
                        sb.append(",");
                    }
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private static void sendNoContent(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String escapeJson(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static class ScoreEntry {
        final String name;
        final int score;
        final String timestamp;

        ScoreEntry(String name, int score, String timestamp) {
            this.name = name;
            this.score = score;
            this.timestamp = timestamp;
        }
    }
}
