# DATABASE_SCHEMA

Room database `musclegain.db`, version 1, schema export in `core/data/schemas/`. Dates are ISO `yyyy-MM-dd`, times `HH:mm:ss`, enums stored by name. Nothing is deleted automatically; there is no retention limit.

| Table | Columns | Indexes / constraints |
|---|---|---|
| user_profile | id (PK, always 1), heightCm, weightKg, age?, goal, createdAtMillis, updatedAtMillis | |
| daily_goals | id (PK auto), effectiveDate, calorieTarget, proteinTarget, carbsTarget, fatTarget, weeklyGymTarget, weeklyRunningTarget, runningDistanceTargetKm | unique(effectiveDate) |
| nutrition_entries | id (PK auto), foodName, mealCategory, calories, proteinG?, carbsG?, fatG?, entryDate, entryTime, notes? | index(entryDate); unique(entryDate, entryTime, foodName, mealCategory) |
| workout_sessions | id (PK auto), activityType, activityDate, durationMinutes?, distanceKm?, intensity?, metUsed?, estimatedGrossKcal?, estimatedActiveKcal?, calculationMethod?, bodyWeightKgUsed?, averageSpeedKmh?, notes?, status | index(activityDate) |
| workout_exercises | id (PK auto), sessionId (FK, cascade delete), name, sets, reps, weightKg | index(sessionId) |
| supplement_entries | id (PK auto), name, amount, unit, entryDate, entryTime, notes? | index(entryDate); unique(entryDate, entryTime, name) |
| body_weight_entries | id (PK auto), weightKg, recordedDate, notes? | unique(recordedDate), one weight per day |
| training_schedule | weekday (PK, 1=Monday..7=Sunday), plannedGym, plannedRunning, plannedRest | |
| reminder_settings | id (PK, always 1), meal/workout/bodyWeight/supplement Enabled and Time | |

Notes
- Duplicate inserts hit the unique indexes and surface as `DUPLICATE_RECORD`.
- All range queries use `BETWEEN` on indexed date columns; no query loads the whole database.
- DataStore (`app_preferences`) holds `onboarding_completed` and `last_backup_at`.
- Migrations: add them to `Migrations.ALL`, bump `DATABASE_VERSION`, commit the new schema JSON. JSON restore currently accepts only backups with the same schema version.
- Backup JSON: `{app, backupVersion, schemaVersion, exportedAtMillis, tables{table: [rows]}}`. Restore validates first, then replaces all data in one transaction.
