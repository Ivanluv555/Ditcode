-- Run with: psql "$DATABASE_URL" -f db/schema/000-init.sql

BEGIN;

\i db/schema/001_extensions.sql
\i db/schema/010_tables.sql
\i db/schema/020_constraints.sql
\i db/schema/030_indexes.sql
\i db/schema/040_triggers.sql

COMMIT;

