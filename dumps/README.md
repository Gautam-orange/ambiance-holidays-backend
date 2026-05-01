# Database snapshots

Periodic full dumps of the local development database, used to sync state
across machines or seed a fresh environment with the same catalog + test data.

## Files

- `snapshot.sql` — most recent full dump of the local dev DB. Contains:
  - Schema (all tables, types, enums, constraints, indexes)
  - All catalog data (cars, tours, day trips, transfer pricing tiers, system settings)
  - Sample agents and a SUPER_ADMIN user
  - Historical bookings + payments accumulated during testing
  - Flyway schema history

## Apply on a new machine

```bash
# 1. Bring up Postgres + Redis
docker compose up -d postgres redis

# 2. Wait for Postgres to be ready
until docker exec ambianceholidays-postgres-1 pg_isready -U ambiance; do sleep 1; done

# 3. Restore the snapshot (the file is idempotent — uses `DROP TABLE IF EXISTS`
#    via `--clean --if-exists` so existing tables are dropped first).
docker exec -i ambianceholidays-postgres-1 \
  psql -U ambiance -d ambiance_holidays < dumps/snapshot.sql

# 4. Start the backend — Flyway will detect the matching schema_history rows
#    and skip migrations.
```

## Refresh the snapshot

When you want to capture the current state of your local DB (e.g. before
sharing across machines):

```bash
docker exec ambianceholidays-postgres-1 \
  pg_dump -U ambiance -d ambiance_holidays \
  --no-owner --no-privileges --clean --if-exists \
  > dumps/snapshot.sql
```

## Notes

- Passwords stored in `users.password_hash` are Argon2id — safe to share
  but they're test passwords (e.g. `Admin@123`, `Agent@123`).
- Sandbox Peach credentials live in env vars, **not** in the DB.
- For a clean install without historical bookings, use Flyway only —
  delete the `dumps/` directory and run the backend; migrations V1–V10
  will recreate the schema and seed catalog data from scratch.
