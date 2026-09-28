# CarbonTrack — Household Carbon Footprint Calculator

Spring Boot REST API for logging household activities, estimating monthly carbon footprints, managing community challenges, tracking participation, and ranking households by month-over-month percentage reduction.

## Requirements
- JDK 21
- MySQL 8.x (or a compatible MySQL server)
- Maven (or use the included Maven wrapper if available)

## 1. Create the database
Run in MySQL Workbench:

```sql
CREATE DATABASE carbon_track;
```

## 2. Configure database credentials
Open `src/main/resources/application.properties`. The defaults are:
- URL: `jdbc:mysql://localhost:3306/carbon_track`
- Username: `root`
- Password: read from the `DB_PASSWORD` environment variable (defaults to blank if unset)

Set `DB_PASSWORD` as an environment variable to match your local MySQL password. Example (PowerShell):
```powershell
$env:DB_PASSWORD = "your_password"
```
Hibernate creates/updates tables with `ddl-auto=update`.

## 3. Run
From the project root:

```powershell
mvn spring-boot:run
```

Or in IntelliJ, open the folder, wait for Maven import, then run `CarbonTrackApplication`.

Base URL: `http://localhost:8080`

## API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/households` | Create household |
| GET | `/api/households` | List households |
| GET | `/api/households/{id}` | Get household |
| PUT | `/api/households/{id}` | Update household |
| DELETE | `/api/households/{id}` | Delete household |
| POST | `/api/activities` | Log a household activity |
| GET | `/api/households/{id}/activities` | List household activity logs |
| DELETE | `/api/activities/{id}` | Delete activity log |
| GET | `/api/footprints/monthly?householdId=1&year=2026&month=9` | Monthly footprint |
| POST | `/api/challenges` | Create a challenge |
| GET | `/api/challenges` | List challenges |
| GET | `/api/challenges/{id}` | Get challenge |
| PUT | `/api/challenges/{id}` | Update challenge |
| DELETE | `/api/challenges/{id}` | Delete challenge |
| POST | `/api/challenges/{id}/participations?householdId=1` | Join challenge |
| GET | `/api/challenges/{id}/participations` | List participants |
| GET | `/api/challenges/{id}/progress?householdId=1` | Check challenge progress |
| GET | `/api/leaderboard?year=2026&month=9` | Monthly reduction leaderboard |

## Example requests (Postman)

### Create household
`POST /api/households`, Body → raw → JSON:
```json
{
  "name": "Green Home",
  "email": "green@example.com",
  "location": "Coimbatore"
}
```

### Log activity
`POST /api/activities`:
```json
{
  "householdId": 1,
  "activityDate": "2026-09-28",
  "commuteMode": "BUS",
  "commuteDistanceKm": 12,
  "electricityKwh": 5.5,
  "wasteKg": 1.2
}
```

### Create a challenge
`POST /api/challenges`:
```json
{
  "title": "September Carbon Cut",
  "description": "Reduce household emissions compared with the previous month.",
  "startDate": "2026-09-01",
  "endDate": "2026-09-30",
  "targetReductionPercent": 10,
  "active": true
}
```

### Join challenge
`POST /api/challenges/1/participations?householdId=1`

## Calculation assumptions

The assignment requires a documented, fixed emission-factor table but does not prescribe numerical values. This implementation therefore uses the following **illustrative project assumptions**, not a claim of official or universally applicable factors. For a formal submission, replace them with the factor table prescribed by your instructor or cited source, and keep the table fixed during a reporting period.

| Activity | Factor | Unit |
|---|---:|---|
| Walking / cycling / none | 0 | kg CO2e per km |
| Bus | 0.089 | kg CO2e per passenger-km |
| Train | 0.041 | kg CO2e per passenger-km |
| Car | 0.192 | kg CO2e per km |
| Motorcycle | 0.103 | kg CO2e per km |
| Electricity | 0.716 | kg CO2e per kWh |
| Waste generated | 0.570 | kg CO2e per kg |

Each activity's estimated emissions are calculated when the activity is logged and stored in `estimated_kg_co2e`.

## Business rules implemented
- Non-negative activity quantities; at least one quantity must be greater than zero.
- Commute distance must be zero when commute mode is `NONE`.
- Challenge end date cannot precede its start date.
- A household can join a given challenge only once and only while the challenge is active and within its date range.
- Monthly leaderboard uses percentage reduction from the immediately previous calendar month, not absolute emissions. If the previous month is zero, reduction is reported as 0% to avoid division by zero.
- Invalid input and missing records return clear JSON error responses through the global exception handler.

## Notes
- The progress endpoint compares the current calendar month with the previous calendar month.
- A month with no logged activities is treated as zero recorded emissions. For meaningful comparisons, log activities consistently.
- The project is a backend API. You can connect any frontend to the endpoints above.

## Frontend
A responsive browser-based dashboard is included at `src/main/resources/static/index.html`.
It is served by the same Spring Boot application, so no separate frontend server or Node.js setup is required. After the backend starts, open `http://localhost:8080/`.

The UI includes overview metrics, household management, activity logging, challenge management and participation, progress display, and the monthly reduction leaderboard.
