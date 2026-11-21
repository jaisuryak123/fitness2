# AI_HANDOFF

## Stage 1: backend foundation (complete)
Delivered: project structure, version catalog, `:core:domain`, `:core:data`, minimal `:app` shell, unit tests, docs, CLAUDE.md.

### Verification status (honest report)
- Verified: `:core:domain` main sources compile with Kotlin 2.0.21, and calculators were smoke-run (running 360.2 kcal gross, walking 154.4, gym 420.0, weekly adherence 57.1%, matching the unit tests). The compile used a minimal stub of `kotlinx.coroutines.flow.combine`/`Flow` because Maven Central was unreachable.
- NOT verified in the build environment (no Android SDK, Gradle distribution or Maven access): Gradle sync, KSP/Room code generation, `:core:data` compilation, JUnit and Robolectric test execution, `:app` build. Dependency versions in `gradle/libs.versions.toml` were chosen as mutually compatible from knowledge, not resolved online; update them in Android Studio if newer stable versions are preferred.
- First action on your machine: `./gradlew test`, fix anything reported, then update this file.

## Stage 2: Android Frontend & UI/UX Presentation Layer (complete)
Delivered by Antigravity:
1. **Modern Dark-Based Athletic Fitness UI Theme**:
   - `ui/theme/Color.kt`: Athletic Dark palette (Deep charcoal `#0F1117`, card surface `#171B24`, `#1F2432`, `#2B3345`), Electric Lime primary accent (`#B4F000`), Electric Cyan (`#38BDF8`), Amber (`#FBBF24`), Rose (`#FB7185`), and distinct macro palette.
   - `ui/theme/Type.kt`: High-contrast typography optimized for numeric dashboard readability.
   - `ui/theme/Theme.kt`: `MuscleGainTheme` with dark-first configuration and dynamic system bar theming.
2. **Navigation & Application Shell**:
   - `ui/navigation/Screen.kt`: 4 primary bottom navigation tabs (Today, History, Weekly, Profile) plus modal routes: Food Entry, Workout Entry, Training Plan, Supplements, Reminders, Backup.
   - `ui/navigation/NavGraph.kt`: Scaffold with bottom navigation bar, smooth backstack navigation, and parameter routing.
   - `MainActivity.kt`: `ComponentActivity` entrypoint binding to `MuscleGainContainer` and initializing notification channels.
3. **Reusable Modular UI Components**:
   - `ui/components/NutritionCard.kt`: Calories consumed, daily calorie target, remaining kcal hero section, macro progress bars with percent indicators, and incomplete macros warning badge.
   - `ui/components/WorkoutCard.kt`: Today's scheduled workout from `TrainingDay`, completion status, completed workouts list, and estimated active calories labeled with `WorkoutCalorieEstimator.ESTIMATE_LABEL`.
   - `ui/components/MacroProgressBar.kt`: Custom animated macro progress bars.
   - `ui/components/QuickActionButtons.kt`: One-tap action grid (Food, Workout, Supplement, Weight).
   - `ui/components/WeeklyCharts.kt`: Canvas-based 7-day Bar Charts for daily calories and protein with target lines, and Weight Trendline chart with delta indicators.
   - `ui/components/CommonDialogs.kt`: Quick body weight logging dialog and delete confirmation dialogs.
   - `ui/components/EmptyState.kt` & `StatCard.kt`: Informative empty states and KPI cards.
4. **Interactive Manual Entry Forms**:
   - `ui/forms/FoodForm.kt`: Food name, meal category chips, numeric inputs for calories, protein, carbs, fat, optional notes, inline error validation, and macro-calorie mismatch warning.
   - `ui/forms/WorkoutForm.kt`: 4 activity types (Gym with dynamic exercise builder for sets/reps/weight, Running with live pace/speed preview, Walking, Other Cardio with intensity levels) and real-time MET calorie preview.
   - `ui/forms/ProfileForm.kt`: Editable physical metrics and daily target goals.
   - `ui/forms/SupplementForm.kt`: Supplement logging with quick suggestion chips from known entries.
5. **Full Application Screens**:
   - `ui/screens/TodayScreen.kt`: Primary dashboard with date stepper, factual progress summary, quick actions, nutrition card, workout card, and body weight widget.
   - `ui/screens/FoodEntryScreen.kt`: Manual food logging form with day's entries list and edit/delete actions.
   - `ui/screens/WorkoutEntryScreen.kt`: Workout logging with activity switcher and day's workout sessions list.
   - `ui/screens/WeeklyScreen.kt`: Monday-to-Sunday weekly performance with week stepper, nutrition bar charts, workout adherence breakdown, and weight trend.
   - `ui/screens/HistoryScreen.kt`: 14-day interactive date calendar strip and comprehensive historical logs with edit/delete support.
   - `ui/screens/ProfileScreen.kt`: Profile metrics, targets, weight history table, supplement logs, and settings shortcuts.
   - `ui/screens/TrainingPlanScreen.kt`: 7-day schedule configuration (planned gym, running, rest days) and weekly target frequency.
   - `ui/screens/NotificationSettingsScreen.kt`: Pre-permission explanation card, POST_NOTIFICATIONS launcher, and scheduled reminder toggles.
   - `ui/screens/BackupScreen.kt`: JSON backup export/restore, CSV table exports, and data reset.
   - `ui/screens/SupplementLogScreen.kt`: Supplement tracking screen.
6. **MVVM ViewModels & Integration**:
   - `viewmodel/ViewModelFactory.kt`: Instantiates ViewModels injecting `MuscleGainContainer`.
   - `viewmodel/TodayViewModel.kt`, `FoodEntryViewModel.kt`, `WorkoutEntryViewModel.kt`, `WeeklyViewModel.kt`, `HistoryViewModel.kt`, `ProfileViewModel.kt`, `TrainingPlanViewModel.kt`, `NotificationViewModel.kt`, `BackupViewModel.kt`.
   - All ViewModels expose `StateFlow` and cleanly connect to repositories and use cases without duplicate calculation or storage logic.
7. **Offline Notification System**:
   - `notification/NotificationHelper.kt`: Notification channel setup and builder.
   - `notification/ReminderWorker.kt`: WorkManager `CoroutineWorker` for offline reminders.
   - `notification/NotificationScheduler.kt`: Schedules periodic reminders for meals, workouts, weight logging, and supplements.
8. **Unit Tests**:
   - `FoodEntryViewModelTest.kt`: Form validation, macro mismatch warning, save & edit logic.
   - `WorkoutEntryViewModelTest.kt`: Activity type switching, dynamic exercise builder, speed/MET preview, validation.
   - `NavigationTest.kt`: Bottom navigation configuration and route parameter generation.

## Verification & Next Steps
- Android Studio / Google AI Studio: Run `./gradlew test` and `./gradlew :app:assembleDebug`.
- Test on emulator or physical Android device.
