# Backend (Java)

This backend provides leaderboard APIs for the Pacman game.

## Stack

- Java (single-file app in `src/Main.java`)
- Built-in Java HTTP server (`com.sun.net.httpserver.HttpServer`)
- CSV file persistence (`data/scores.csv`)
- Docker container for Fly.io deploy

## What It Does

- Starts an HTTP server on `PORT` (default `8080`)
- Exposes API endpoints:
  - `GET /api/health`
  - `GET /api/scores`
  - `POST /api/scores`
- Stores leaderboard entries in memory and persists to `data/scores.csv`
- Keeps top 10 scores sorted descending
- Handles CORS for frontend requests

## Request/Response

### Health

`GET /api/health`

Response:

```json
{"status":"ok"}
```

### List scores

`GET /api/scores`

Response:

```json
[
  {"name":"PLAYER","score":500,"timestamp":"2026-04-01T00:00:00Z"}
]
```

### Save score

`POST /api/scores`

Body:

```json
{"name":"PLAYER","score":500}
```

Response:

```json
{"saved":true}
```

## Local Run

From this `backend` folder:

```bash
sh run-local.sh
```

This script:

- Creates `out/` and `data/`
- Compiles `src/Main.java` to `out/`
- Starts the server on `PORT` (default `8080`)

Test quickly:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/scores
```

## Fly.io Hosting

This backend is configured for Fly.io via `fly.toml` and `Dockerfile`.

Current Fly app:

- App name: `pacman-java-backend-lingering-flower-7921`
- URL: `https://pacman-java-backend-lingering-flower-7921.fly.dev`
- Health URL: `https://pacman-java-backend-lingering-flower-7921.fly.dev/api/health`

Deploy from this folder:

```bash
fly deploy --remote-only
```

Check status:

```bash
fly status
```

## Notes

- File storage in Fly containers is ephemeral unless you attach a volume.
- For durable scores across restarts/migrations, use a Fly volume or external database.
