# pacman-fullstack-java

JavaScript-first Pacman frontend template based on:
- https://github.com/weibenfalk/vanilla-js-pacman

Project name: pacman-fullstack-java

This project adds a Java backend for leaderboard APIs and is prepared for:
- Vercel: frontend deployment
- Fly.io: backend deployment

If you do not want a Fly account, the frontend still works on Vercel with a browser-local leaderboard fallback.

## Project Structure

- frontend: vanilla JavaScript Pacman game (Parcel)
- backend: Java HTTP server API for health + score leaderboard
- vercel.json: Vercel static deploy config for the frontend build output
- scripts: helper scripts for prereq checks, local run, and deploy

## Backend API

- GET /api/health
- GET /api/scores
- POST /api/scores

Example POST body:

```json
{
  "name": "PLAYER",
  "score": 1230
}
```

## Local Run

### Prerequisites

- Node.js + npm
- Java 21+ (or Java 17+ should also work)

### 1) Start backend

```bash
cd backend
sh run-local.sh
```

Backend runs at http://localhost:8080

### 2) Start frontend

In a new terminal:

```bash
cd frontend
cp .env.example .env
sh run-local.sh
```

Frontend runs with Parcel dev server (typically http://localhost:1234).

### Optional one-command local start

From project root:

```bash
sh scripts/check-prereqs.sh
sh scripts/run-local.sh
```

## Deploy Frontend to Vercel

1. Push this folder to a GitHub repository.
2. In Vercel, import the repo.
3. Vercel reads vercel.json and builds frontend.
4. Add environment variable in Vercel project settings:

- PARCEL_API_BASE = your Fly backend URL, for example:
- https://pacman-java-backend.fly.dev

5. Redeploy after setting env var.

No Fly account path:
1. Skip PARCEL_API_BASE.
2. Deploy only frontend to Vercel.
3. Leaderboard uses localStorage in each browser.

Or deploy with CLI from project root:

```bash
sh scripts/deploy-vercel.sh
```

## Deploy Backend to Fly.io

From backend folder:

```bash
cd backend
fly launch --copy-config --no-deploy
fly deploy
```

Or deploy with CLI helper from project root:

```bash
sh scripts/deploy-fly.sh
```

Notes:
- fly.toml is included and uses internal port 8080.
- App name in fly.toml is pacman-java-backend; change it if already taken.

## Environment Variables

Frontend:
- PARCEL_API_BASE: backend base URL, defaults to http://localhost:8080

Backend:
- PORT: listen port, defaults to 8080

## Current Machine Status

Node, npm, Java, fly CLI, and Vercel CLI are installed.
