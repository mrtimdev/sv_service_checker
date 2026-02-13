# PM Planning Migration (Manual)

This migration adds the **Planning Repair & Maintenance** tables to an old schema.

## Files
- SQL migration: `src/main/resources/sql/planning_repair_maintenance_migration.sql`
- Reference schema: `src/main/resources/sql/planning_repair_maintenance.sql`

## Steps (Manual)
1. **Backup the database**
   - Use your normal backup process before running any migration.
2. **Run the migration SQL**
   - Execute `src/main/resources/sql/planning_repair_maintenance_migration.sql` on the target database.
3. **Verify tables**
   - Run:
     - `SHOW TABLES LIKE 'planning_repair_maintenance%';`
4. **Verify indexes (optional)**
   - Run:
     - `SHOW INDEX FROM planning_repair_maintenance;`
     - `SHOW INDEX FROM planning_repair_maintenance_attachments;`

## Notes
- This is a **new feature**; no data backfill is required.
- The SQL uses `IF NOT EXISTS`, so it is safe to re-run (idempotent).
