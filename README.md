# WaterSec HydroLens Backend

HydroLens is an AI-assisted hotel water Digital Twin demonstrator developed for WaterSec. It generates a plausible hotel configuration, builds a visual water network, runs deterministic water-flow and leak scenarios, and calculates their operational and financial impact.

The application is intended for internal WaterSec demonstrations during hotel pitches. It is a prototype, not a production monitoring or hydraulic-control system.

## How the system works

1. The user selects one of six hotel archetypes in the React frontend.
2. The frontend asks this Spring Boot API to create a temporary Digital Twin session.
3. Llama 3, accessed through Hugging Face, generates a schema-constrained hotel specification.
4. Java validates component types, units, quantities, water-consumption ranges, occupancy, efficiency, and water tariff.
5. Deterministic Java engines calculate normal demand, hourly profiles, events, water loss, and cost. The LLM never performs these calculations.
6. The frontend visualizes the network and presents the business impact.

Supported archetypes:

- City Hotel
- Business Hotel
- Resort
- Boutique Hotel
- Aparthotel
- Luxury Resort

Supported simulation events include continuous leaks, pipe bursts, high occupancy, heat waves, and water-supply interruptions.

## Architecture

```text
React/Vite frontend (localhost:5173)
                |
                | REST/JSON
                v
Spring Boot backend (localhost:8081)
        |                       |
        |                       +--> Deterministic simulation and cost engines
        v
Hugging Face / Llama 3
(hotel configuration only)
```

The backend does not use PostgreSQL, MongoDB, or another database. Hotel specifications, baselines, current scenarios, and event history are held in application memory. They disappear when their session expires, is deleted, or the backend restarts.

Default session settings:

- Time to live: 60 minutes
- Cleanup interval: 5 minutes
- Maximum concurrent sessions: 100
- Maximum retained event history: controlled by the session implementation

## Technology

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Validation
- Spring Security
- Maven Wrapper
- Hugging Face OpenAI-compatible inference API
- Docker

The companion frontend uses React, TypeScript, Vite, Axios, Tailwind CSS, React Router, XYFlow, Recharts, Framer Motion, and Lucide icons.

## Prerequisites

For local backend development:

- Java 17 or later
- Internet access when using Hugging Face inference
- A Hugging Face token authorized to use Inference Providers

For the frontend:

- Node.js and npm

Docker Desktop can be used instead of a local Java installation for the backend.

## Environment configuration

Copy `.env.example` to `.env` in the backend root:

```powershell
Copy-Item .env.example .env
```

Then add your real Hugging Face token to `.env`:

```properties
HF_TOKEN=hf_your_token_here
```

Never commit `.env` or a real token. The repository should contain only `.env.example` with safe placeholders.

### AI modes

Use Hugging Face and Llama 3:

```properties
HYDROLENS_AI_GATEWAY_MODE=huggingface-llama
```

Run without remote AI, using the deterministic seeded hotel planner:

```properties
HYDROLENS_AI_GATEWAY_MODE=local-no-ml
```

In both modes, water and leak calculations remain deterministic.

## Run the backend locally

From the backend directory:

```powershell
cd D:\WaterSec_Internship\waterSec_HydroLens_Backend
.\mvnw.cmd spring-boot:run
```

The API starts at:

```text
http://localhost:8081/api/v1
```

Check the AI configuration:

```text
GET http://localhost:8081/api/v1/ai/status
```

If port `8081` is already occupied, stop the previous backend process before starting another instance.

## Run the frontend with the backend

The frontend is maintained in a separate repository/directory. Its API configuration is:

```properties
VITE_API_URL=http://localhost:8081/api/v1
```

From the frontend directory:

```powershell
cd D:\WaterSec_Internship\waterSec_HydroLens_Frontend
npm install
npm run dev
```

Open the URL printed by Vite, normally:

```text
http://localhost:5173
```

Run the backend before generating a hotel. The frontend stores only the temporary session ID in browser `sessionStorage`; the backend remains the source of truth for the generated hotel and simulation state.

The backend permits configured local Vite origins. Additional origins can be supplied as a comma-separated environment variable:

```properties
HYDROLENS_ALLOWED_ORIGINS=https://your-frontend.example.com
```

## Run the backend with Docker

Create the backend `.env`, then run:

```powershell
docker compose up --build
```

Stop it with:

```powershell
docker compose down
```

The included Compose file builds and exposes only the backend on port `8081`. The frontend currently runs separately through Vite; it needs its own Dockerfile or deployment configuration before both applications can be launched as a complete container stack.

## Main API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/ai/status` | Show configured AI gateway and model |
| `GET` | `/api/v1/hotel-archetypes` | List supported hotel types |
| `POST` | `/api/v1/digital-twin-sessions` | Generate a hotel and create a temporary session |
| `GET` | `/api/v1/digital-twin-sessions/{id}` | Retrieve a session |
| `DELETE` | `/api/v1/digital-twin-sessions/{id}` | Delete a session |
| `POST` | `/api/v1/digital-twin-sessions/{id}/baseline` | Run deterministic normal flow |
| `POST` | `/api/v1/digital-twin-sessions/{id}/events` | Generate and calculate an event |
| `POST` | `/api/v1/digital-twin-sessions/{id}/scenario/reset` | Clear the current scenario |

## Security

The internal demonstration defaults to a public API:

```properties
WATERSEC_PUBLIC_API=true
```

For HTTP Basic authentication, set:

```properties
WATERSEC_PUBLIC_API=false
WATERSEC_API_USERNAME=watersec
WATERSEC_API_PASSWORD=replace-with-a-strong-password
```

Do not expose the demonstration configuration directly to the public internet without reviewing authentication, HTTPS, origin restrictions, rate limiting, and secret management.

## Tests

Run the backend tests:

```powershell
.\mvnw.cmd test
```

Frontend quality checks are run from the frontend repository:

```powershell
npm run typecheck
npm run lint
npm run build
```

## Troubleshooting

### The frontend displays a backend network error

- Confirm the backend is running on port `8081`.
- Confirm `VITE_API_URL` points to `http://localhost:8081/api/v1`.
- Confirm the frontend origin is included in `HYDROLENS_ALLOWED_ORIGINS`.
- Restart Vite after changing a frontend environment variable.

### Hugging Face generation fails

- Confirm `HF_TOKEN` is present and valid.
- Confirm the token can call Hugging Face Inference Providers.
- Check `GET /api/v1/ai/status`.
- Read the backend error message for schema or domain-validation details.
- Use `local-no-ml` when remote inference is not required.

### Old or unrealistic values are still visible

Temporary sessions retain the hotel specification generated when they were created. Restart the backend or generate a new twin after changing prompts or validation rules.

## Repository relationship

The backend and frontend can be stored in separate GitHub repositories. Each repository should keep its own source, tests, README, and build configuration. Container images may be published separately to Docker Hub or GitHub Container Registry, but the source code should remain version-controlled in GitHub.

