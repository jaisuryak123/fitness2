# CALCULATION_RULES

All calculations live in `:core:domain` (`calc/`). Frontend code only displays the results.

## Nutrition
- Daily totals sum the entries of one date. A macro total is the sum of recorded values only; if no entry recorded it, the total is null. Calories are required per entry.
- Remaining = target - consumed (negative means over target). Completion % = consumed / target x 100, one decimal.
- A day with no entries has null totals, remaining and completion.
- Consistency check (warning only): protein 4, carbs 4, fat 9 kcal/g. Warns when the implied energy differs from the entered calories by more than max(50 kcal, 20%). Entered calories are never changed.
- Sums use BigDecimal and are rounded to 2 decimals.

## Workout calories (estimates, never exact)
- Gross kcal = MET x 3.5 x bodyWeightKg x minutes / 200.
- Active kcal = (MET - 1) x 3.5 x bodyWeightKg x minutes / 200.
- Body weight: latest body-weight entry on or before the workout date, else profile weight. Height is never used.
- Running and walking need distance and duration. Missing either gives no estimate (null) plus a warning. Average speed = km / (minutes / 60).
- Running MET by speed (km/h, highest threshold not above the speed): 0 -> 6.0, 8.0 -> 8.3, 9.7 -> 9.8, 11.3 -> 11.0, 12.9 -> 11.8, 14.5 -> 12.8, 16.1 -> 14.5, 17.7 -> 16.0, 19.3 -> 19.0.
- Walking MET: 0 -> 2.0, 3.2 -> 2.8, 4.0 -> 3.0, 4.8 -> 3.5, 5.6 -> 4.3, 6.4 -> 5.0, 7.2 -> 7.0, 8.0 -> 8.3.
- Gym by intensity: LOW 3.5, MODERATE 5.0, HIGH 6.0. Other cardio: LOW 3.5, MODERATE 5.5, HIGH 8.0. Missing duration or intensity gives no estimate.
- Method ids: `MET_SPEED_TABLE_V1`, `MET_INTENSITY_V1`. If the tables change, add a new method id; old rows keep their stored values.
- Only COMPLETED sessions carry estimates and count in totals.

## Separation rules
Food intake, estimated exercise expenditure and calorie targets are independent. Missed sessions never lower targets; exercise calories are never added to the food allowance.

## Weekly summary (Monday to Sunday)
- Averages are over days that have nutrition entries (macro averages over days where that macro was recorded). Null when there is no data.
- Protein days achieved: days whose protein total reached the goal effective on that date (latest goal with effectiveDate on or before the date; earliest goal or built-in defaults if none).
- Counts: gym and running sessions with status COMPLETED. Distance and duration sum recorded values. Strength volume = sets x reps x kg over completed gym exercises (null if none).
- Weight change = last minus first body weight inside the week; null with fewer than 2 measurements.
- Tracking completion % = days with nutrition entries (up to today) / days elapsed in the week (up to today).

## Adherence
- Planned sessions come from the training schedule (a day may plan gym and/or running). Per planned session: COMPLETED if a completed session of that type exists on that date; MISSED if the date is before today; PENDING otherwise (today and future).
- Adherence % = completed / (completed + missed). Null when nothing is due yet.
- Rest day (planned rest, no sessions) is flagged `isRestDay` and never counts as missed. Sessions done on unplanned days count in totals but not in adherence.
- Default goals: gym 5 per week, running 2 per week, 5 km per run. Default schedule: Mon-Fri gym, Tue and Sat running, Sunday rest. Default nutrition targets (2800 kcal, 140 g protein, 380 g carbs, 80 g fat) are placeholders the user must edit.
