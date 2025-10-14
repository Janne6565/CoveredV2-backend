# CoveredV2

CoveredV2 is a Spring Boot 3 application (Java 21) for managing a catalog of games and their covers, integrating with Steam and SteamGridDB to fetch metadata and artwork.

## Tech Stack
- Java 21
- Spring Boot 3 (Web, WebFlux, Data JPA)
- H2 (runtime)
- Lombok
- Maven

## Project Structure
- controllers: REST endpoints (games, covers, root)
- services: business logic and external API integrations (Steam, SteamGridDB)
- entities: JPA entities (Game, Cover)
- repositories: Spring Data JPA repositories
- dtos: request/response models for external APIs
- config: configuration/utilities
- resources: application.yaml, test profile

## Prerequisites
- Java 21
- Maven 3.9+
- API tokens/keys as environment variables (recommended)

## Configuration
Default profile uses H2. Override via environment variables or application.yaml.

Common properties:
- Spring datasource (if switching from H2)
- External API credentials:
    - Steam Web API token
    - SteamGridDB API key

Store secrets in environment variables or a local, untracked file.

## Build & Run
- Build: `mvn clean package`
- Run (dev): `mvn spring-boot:run`
- Run (jar): `java -jar target/CoveredV2-0.0.1-SNAPSHOT.jar`

App runs at `http://localhost:8080` by default.

## API Overview

Base path: defined in a base controller constant (referred to here as `{BASE_URL}`).

### Root
- GET `{BASE_URL}/`
    - Description: Basic health/info endpoint.
    - Request: No body or params.
    - Response: `text/plain` — String greeting.

### Games
- GET `{BASE_URL}/games`
    - Description: List games with pagination and optional search.
    - Query params:
        - `page` (integer, optional) — zero-based page index.
        - `size` (integer, optional, default 50) — page size.
        - `sort` (string, optional) — e.g., `name,asc`.
        - `search` (string, optional) — free-text search.
    - Response: `application/json` — Spring `Page<Game>`:
        - `content` (Game[]) and standard Spring page metadata.

- GET `{BASE_URL}/games/player/{playerId}`
    - Description: List games for a specific player.
    - Path params:
        - `playerId` (long, required).
    - Response: `application/json` — `Game[]`.

- GET `{BASE_URL}/games/family/{userId}`
    - Description: List games available via Steam Family Sharing for a user.
    - Path params:
        - `userId` (long, required).
    - Query params:
        - `token` (string, required) — Steam API token for the user.
    - Response: `application/json` — `Game[]`.

- POST `{BASE_URL}/games/steam/{steamGameId}`
    - Description: Load/import a game from Steam into the catalog.
    - Path params:
        - `steamGameId` (long, required).
    - Request body:
        - `text/plain` — game name (string).
    - Response: `application/json` — `Game`.

### Covers
- GET `{BASE_URL}/covers`
    - Description: List covers with pagination.
    - Query params:
        - `page` (integer, optional) — zero-based page index.
        - `size` (integer, optional, default 50) — page size.
        - `sort` (string, optional).
    - Response: `application/json` — Spring `Page<Cover>`:
        - `content` (Cover[]) and standard Spring page metadata.

- GET `{BASE_URL}/covers/game/{gameId}`
    - Description: Retrieve all covers for a specific game.
    - Path params:
        - `gameId` (string, required) — Game UUID.
    - Response: `application/json` — `Cover[]`.

## Data Models

JSON uses snake_case field names.

### Game (JSON example)
```json
{
  "uuid": "a2e5f5e4-3f6b-4d2d-9e1b-1a2b3c4d5e6f",
  "name": "Half-Life 2",
  "steam_id": 220,
  "header_image_url": "https://cdn.example/hl2/header.jpg",
  "capsule_image_url": "https://cdn.example/hl2/capsule.jpg",
  "library_image_url": "https://cdn.example/hl2/library.png",
  "time_of_last_cover_fetch": 1712345678901,
  "steam_grid_db_missing": false,
  "game_fetched_at": 1712300000000
}
```
- uuid: string (UUID)
- name: string
- steam_id: number (long)
- header_image_url: string (may be large text)
- capsule_image_url: string
- library_image_url: string
- time_of_last_cover_fetch: number (epoch millis)
- steam_grid_db_missing: boolean
- game_fetched_at: number (epoch millis)

### Cover (JSON example)
```json
{
  "uuid": "f1c0a8d2-7f4a-4c1e-9d77-1234567890ab",
  "steam_grid_db_id": 123456,
  "style": "alternate",
  "game_uuid": "a2e5f5e4-3f6b-4d2d-9e1b-1a2b3c4d5e6f",
  "width": 600,
  "height": 900,
  "nsfw": false,
  "humor": false,
  "notes": "High-res portrait cover",
  "mime": "image/jpeg",
  "language": "en",
  "thumb": "https://cdn.example/cover/thumb.jpg",
  "url": "https://cdn.example/cover/original.jpg"
}
```

- uuid: string (UUID)
- steam_grid_db_id: number (long)
- style: string
- game_uuid: string (UUID of related Game)
- width: number (int)
- height: number (int)
- nsfw: boolean
- humor: boolean
- notes: string (may be large text)
- mime: string
- language: string (e.g., "en")
- thumb: string (URL)
- url: string (URL)

## Development Notes
- Lombok is used; enable annotation processing in your IDE.
- WebFlux may be used for non-blocking external calls alongside MVC REST controllers.
- H2 is included for quick local development.

## Testing
- Run tests: `mvn test`
- A test profile and basic tests are included.

## Environment Variables (examples)
- `STEAM_API_TOKEN=your_steam_token`
- `STEAMGRIDDB_API_KEY=your_sgdb_key`
- `SPRING_PROFILES_ACTIVE=dev`
- `SPRING_DATASOURCE_URL=jdbc:h2:mem:coveredv2`

## Packaging
- Uses `spring-boot-maven-plugin` for building executable jars.

## License
Add a LICENSE file with your chosen license.