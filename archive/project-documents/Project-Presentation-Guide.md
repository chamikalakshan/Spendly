# Spendly Project Presentation Guide

## 1. Project Summary

Spendly is a personal finance tracking Android application built with Kotlin, Jetpack Compose, Material Design 3, MVVM architecture, Room, Firebase Authentication, Cloud Firestore, Hilt dependency injection, and WorkManager.

The main purpose of the app is to help a user manage daily finances by recording income, expenses, savings goals, profile preferences, and analytics. The implemented app is designed as a local-first application. This means that the app stores user data in the local Room database first and then synchronizes that data with Firestore when the network is available.

Core features:

- User login with Firebase Authentication
- Create account with name, email, password, and default currency
- Dashboard with total balance, monthly income, monthly expenses, monthly net savings, yearly net savings, primary goal, and recent transactions
- Transaction history with month filtering, income/expense filters, edit, and delete
- Add income with source selection, currency conversion, crypto support, recurring flag, date, and note
- Add expense with category selection, payment method, expense type, currency conversion, date, and note
- Analytics with income/expense totals, spending by category, committed vs discretionary split, monthly overview, and income sources
- Goal tracking with add goal, edit goal, goal details, add savings, progress, icon picker, and primary goal support
- Profile with avatar, profile image picker, default currency, theme selector, exchange rate dialog, change password, logout, and delete account
- Background sync using WorkManager

High-level architecture:

```text
Compose UI Screen
    -> ViewModel
        -> Repository Interface
            -> Repository Implementation
                -> Room DAO
                -> Firestore
```

Example flow for adding income:

```text
AddIncomeScreen
    -> AddIncomeViewModel
        -> IncomeRepository
            -> IncomeRepositoryImpl
                -> IncomeDao.insert()
                -> Firestore users/{uid}/income/{incomeId}
```

## 2. Technology Stack

- Language: Kotlin
- UI toolkit: Jetpack Compose
- Design system: Material Design 3
- Architecture: MVVM with Repository pattern
- Local database: Room
- Cloud backend: Firebase Authentication and Cloud Firestore
- Dependency injection: Hilt
- Background processing: WorkManager
- Async programming: Kotlin Coroutines and Flow
- Navigation: Navigation Compose
- Build system: Gradle Kotlin DSL

Important Gradle dependencies are configured in:

```text
app/build.gradle.kts
gradle/libs.versions.toml
```

Important dependency groups:

- `androidx.compose.*` for UI
- `androidx.navigation.compose` for navigation
- `androidx.room.*` for local database
- `firebase-auth-ktx` and `firebase-firestore-ktx` for Firebase
- `hilt-android`, `hilt-navigation-compose`, and `hilt-work` for dependency injection
- `androidx.work-runtime-ktx` for WorkManager sync

## 3. Files And Folder Structure

Main source folder:

```text
app/src/main/kotlin/com/spendly/financetracker
```

### Root Application Files

```text
MainActivity.kt
SpendlyApplication.kt
```

#### MainActivity.kt

This is the Android entry point.

Responsibilities:

- Enables edge-to-edge layout.
- Creates the main `FinanceViewModel` using Hilt.
- Observes the main app state.
- Reads the user selected theme mode from the profile.
- Applies System, Light, or Dark theme.
- Loads `FinanceTrackerApp`.

Important logic:

```text
ThemeMode.SYSTEM -> follows device theme
ThemeMode.LIGHT  -> forces light mode
ThemeMode.DARK   -> forces dark mode
```

#### SpendlyApplication.kt

This is the custom application class.

Responsibilities:

- Enables Hilt for the whole application using `@HiltAndroidApp`.
- Provides WorkManager configuration with `HiltWorkerFactory`.
- Schedules periodic sync when the app starts.

### Data Layer

```text
data/
```

The data layer contains local database code, Firebase communication, repositories, models, and external service code.

#### data/local/entity

Room table models:

```text
IncomeEntryEntity.kt
ExpenseEntryEntity.kt
SavingsGoalEntity.kt
UserProfileEntity.kt
```

These files define how data is stored in SQLite through Room.

Main tables:

- `income_entries`
- `expense_entries`
- `savings_goals`
- `user_profiles`

Important implementation detail:

- Money is stored as `Long` cents using fields like `amountCents`, `targetCents`, and `savedCents`.
- User ownership is stored with `userId` or `uid`.
- Sync state is tracked with `isSynced`.
- Event date is stored as `dateMillis`.
- Creation/update audit fields are stored as `createdAtMillis` and `updatedAtMillis`.

#### data/local/dao

Room DAO interfaces:

```text
IncomeDao.kt
ExpenseDao.kt
GoalDao.kt
UserProfileDao.kt
```

DAO responsibility:

- Insert records
- Update records
- Delete records
- Observe records by user
- Query monthly records
- Query unsynced records
- Mark records as synced

Example DAO flow:

```text
ViewModel calls repository
Repository calls DAO
DAO writes or reads Room database
Room emits Flow updates back to ViewModel
UI updates automatically
```

#### data/local/db/SpendlyDatabase.kt

This is the main Room database class.

Responsibilities:

- Registers all entities.
- Provides DAO accessors.
- Defines migrations from database version 1 to version 5.

Current database version:

```text
version = 5
```

Migration summary:

- Version 1 to 2: Added currency, crypto, profile, and goal fields.
- Version 2 to 3: Added `categorySettingsJson`.
- Version 3 to 4: Added `iconKey` to goals.
- Version 4 to 5: Added `themeMode` to user profile.

Presentation point:

Room migrations prevent destructive database resets when the schema changes.

#### data/model

Domain models:

```text
FinanceTransaction.kt
SavingsGoal.kt
UserProfile.kt
UserSession.kt
```

These models are used by ViewModels and UI screens.

Important models:

- `FinanceTransaction`: common model for both income and expense.
- `TransactionDraft`: input model used when saving income or expense.
- `SavingsGoal`: goal model used by goal screens.
- `GoalDraft`: input model used when creating or editing goals.
- `UserProfile`: profile preferences and user settings.
- `UserSession`: Firebase session summary.

#### data/repository

Repository interfaces and shared repositories:

```text
AuthRepository.kt
IncomeRepository.kt
ExpenseRepository.kt
GoalRepository.kt
UserRepository.kt
TransactionRepository.kt
FirebaseAuthRepository.kt
FirebaseTransactionRepository.kt
```

Repository pattern purpose:

- UI does not directly know about Room or Firebase.
- ViewModels call repository interfaces.
- Repository implementations decide whether to read/write Room, Firestore, or both.

`FirebaseTransactionRepository.kt` combines income and expenses into one transaction stream:

```text
IncomeRepository.observeIncome()
ExpenseRepository.observeExpenses()
combine both lists
sort by dateMillis
return List<FinanceTransaction>
```

#### data/remote

Firestore-backed repository implementations:

```text
IncomeRepositoryImpl.kt
ExpenseRepositoryImpl.kt
GoalRepositoryImpl.kt
UserRepositoryImpl.kt
```

These files contain the main backend communication logic.

General save pattern:

```text
1. Create or update Room entity.
2. Insert entity into local Room database with isSynced = false.
3. Try to write the entity to Firestore.
4. If Firestore write succeeds, mark local entity as synced.
5. If Firestore write fails, keep local entity unsynced for later sync.
```

Firestore structure used by the code:

```text
users/{uid}/profile/main
users/{uid}/income/{incomeId}
users/{uid}/expenses/{expenseId}
users/{uid}/goals/{goalId}
```

Important note for presentation:

The current code uses the above collection structure. The current `firestore.rules` file still appears to describe an older `users/{uid}/transactions` structure, so Firestore rules should be updated before production/demo deployment.

#### data/service

External API services:

```text
CurrencyRateService.kt
CryptoRateService.kt
```

Responsibilities:

- `CurrencyRateService` fetches fiat exchange rates.
- `CryptoRateService` fetches crypto prices from CoinGecko.
- Both use HTTP calls and in-memory caching.
- Manual rates are still allowed when API calls fail.

#### data/firebase/FirebaseBootstrap.kt

Checks whether Firebase is configured.

If `google-services.json` is missing or Firebase cannot initialize, the app shows the Firebase setup screen instead of crashing.

### Dependency Injection

```text
di/
```

Files:

```text
AppModule.kt
RepositoryModule.kt
```

#### AppModule.kt

Provides:

- `FirebaseAuth`
- `FirebaseFirestore`
- `SpendlyDatabase`
- DAOs
- `WorkManager`

Also enables Firestore local persistence and unlimited cache.

#### RepositoryModule.kt

Binds interfaces to concrete classes:

```text
AuthRepository -> FirebaseAuthRepository
IncomeRepository -> IncomeRepositoryImpl
ExpenseRepository -> ExpenseRepositoryImpl
GoalRepository -> GoalRepositoryImpl
UserRepository -> UserRepositoryImpl
TransactionRepository -> FirebaseTransactionRepository
```

Presentation point:

Hilt keeps object creation centralized and makes ViewModels easier to maintain.

### UI Layer

```text
ui/
```

#### ui/FinanceTrackerApp.kt

This is the main Compose app shell.

Responsibilities:

- Shows splash screen first.
- Checks Firebase setup.
- Shows loading state.
- Routes unauthenticated users to login/create account.
- Routes authenticated users to main screens.
- Owns bottom navigation.
- Owns shared floating add buttons.
- Applies navigation transitions.
- Shows snackbar messages.
- Shows initial income dialog for new users.

Important navigation behavior:

```text
Unauthenticated flow:
AuthScreen -> CreateAccountScreen

Authenticated flow:
Home
History
Analytics
Goals
Profile
Add Income
Add Expense
Add Goal
Goal Details
Edit Goal
```

#### ui/navigation

Files:

```text
Screen.kt
BottomNavItem.kt
SpendlyBottomNavBar.kt
SpendlyNavGraph.kt
```

Responsibilities:

- Defines route names.
- Defines bottom navigation routes.
- Provides navigation item metadata.

Important route note:

The History page uses internal route name `events` for compatibility:

```text
Screen.Events.route = "events"
```

The visible label is History.

#### ui/components

Reusable UI components:

```text
AppBottomNavigation.kt
SpendlyAddActionMenu.kt
SpendlyDesign.kt
SpendlyDesignTokens.kt
SpendlyMonthPicker.kt
TransactionListItem.kt
SummaryCard.kt
SummaryPanel.kt
GoalCard.kt
ProfileStat.kt
HeaderSection.kt
```

Important components:

- `AppBottomNavigation`: main bottom navigation.
- `SpendlyAddActionMenu`: shared floating add menu for income/expense.
- `SpendlyFab`: shared circular floating button.
- `SpendlyMonthPicker`: shared month dropdown used by History and Analytics.
- `TransactionListItem`: reusable transaction row with expand, edit, and delete actions.
- `SpendlyDesignTokens`: shared spacing, sizing, and radius constants.

Presentation point:

Reusable components improve UI consistency and reduce duplicated UI code.

#### ui/theme

Files:

```text
Color.kt
Theme.kt
ThemeMode.kt
Type.kt
```

Responsibilities:

- Defines Spendly color constants.
- Defines light and dark Material 3 color schemes.
- Defines typography.
- Defines theme mode values: `SYSTEM`, `LIGHT`, `DARK`.

Theme mode is stored in the user profile and synced through Firestore.

#### ui/util

Helper files:

```text
AmountVisualTransformation.kt
CategorySettings.kt
GoalIconUtils.kt
MonthOptions.kt
UiUtils.kt
```

Responsibilities:

- Format money with commas.
- Format dates.
- Generate month dropdown options.
- Parse and serialize category/source settings JSON.
- Suggest goal icons from goal names.
- Apply amount input formatting.

### ViewModel Layer

```text
ui/viewmodel/
```

Files:

```text
FinanceViewModel.kt
FinanceUiState.kt
CreateAccountViewModel.kt
AddIncomeViewModel.kt
AddExpenseViewModel.kt
TransactionsViewModel.kt
AnalyticsViewModel.kt
GoalsViewModel.kt
HomeViewModel.kt
ProfileViewModel.kt
```

ViewModel responsibilities:

- Hold screen state.
- Validate form inputs.
- Convert UI input into domain drafts.
- Call repositories.
- Expose loading, error, and success state.
- Keep business logic out of Composable screens as much as possible.

### Worker And Sync

```text
worker/SpendlySyncWorker.kt
util/SyncManager.kt
```

`SpendlySyncWorker` syncs:

- user profile
- income
- expenses
- goals

`SyncManager` schedules:

- periodic sync every 15 minutes
- immediate sync after login/account creation

Sync flow:

```text
WorkManager starts SpendlySyncWorker
Worker gets current Firebase uid
Worker calls each repository syncWithFirestore(uid)
Repositories push unsynced Room rows to Firestore
Repositories fetch Firestore rows back into Room
```

## 4. Domain Division For Group Members

The application can be explained as four project domains. Each member owns UI files, ViewModels, repositories, DAO/entity usage, and backend communication related to that domain.

## Domain 1: Chamika

Area:

```text
Profile page
Dashboard page
Related UI components
Related backend logic and DB communication
```

### Main Files

Dashboard:

```text
ui/screen/home/HomeScreen.kt
ui/viewmodel/FinanceUiState.kt
ui/viewmodel/FinanceViewModel.kt
ui/components/SummaryCard.kt
ui/components/SummaryPanel.kt
ui/components/HeaderSection.kt
ui/components/TransactionListItem.kt
ui/components/AppBottomNavigation.kt
```

Profile:

```text
ui/screen/profile/ProfileScreen.kt
ui/viewmodel/ProfileViewModel.kt
ui/viewmodel/FinanceViewModel.kt
ui/theme/ThemeMode.kt
data/model/UserProfile.kt
data/local/entity/UserProfileEntity.kt
data/local/dao/UserProfileDao.kt
data/remote/UserRepositoryImpl.kt
data/repository/UserRepository.kt
util/Mappers.kt
```

### Dashboard Logic Explanation

`HomeScreen.kt` displays the main financial summary after login.

Step-by-step logic:

1. Receives `FinanceUiState` from `FinanceViewModel`.
2. Reads the logged-in user's profile name and email.
3. Displays green dashboard header with greeting and avatar initials.
4. Displays total balance using `state.balanceCents`.
5. Displays monthly income using `state.currentMonthIncomeCents`.
6. Displays monthly expenses using `state.currentMonthExpenseCents`.
7. Displays monthly net savings using `state.currentMonthNetSavingsCents`.
8. Displays yearly net savings using `state.currentYearSavingsCents`.
9. Displays the primary goal if available.
10. Displays recent transactions using `state.recentTransactions`.
11. Navigates to Profile when avatar is clicked.
12. Navigates to History when "See all" is clicked.
13. Navigates to Goal Details when goal card is clicked.

Dashboard calculations are mostly prepared in `FinanceUiState.kt`.

Important computed fields:

- `balanceCents`
- `currentMonthIncomeCents`
- `currentMonthExpenseCents`
- `currentMonthNetSavingsCents`
- `savingsRate`
- `currentYearSavingsCents`
- `recentTransactions`
- `primaryGoal`
- `primaryGoalMonthlyNeedCents`

Data source:

```text
FinanceViewModel
    -> UserRepository.observeProfile(uid)
    -> TransactionRepository.observeTransactions(uid)
    -> GoalRepository.observeGoals(uid)
```

### Profile Logic Explanation

`ProfileScreen.kt` manages profile preferences and account actions.

Step-by-step logic:

1. Reads profile and session from `FinanceUiState`.
2. Displays profile image if `profileImageUri` exists.
3. Falls back to initials avatar if no image exists.
4. Allows profile image selection through Android photo picker.
5. Allows editing name and profile image.
6. Allows changing default currency.
7. Allows changing appearance: System, Light, or Dark.
8. Allows opening exchange rate settings dialog.
9. Allows password change.
10. Allows logout.
11. Allows account deletion.

Profile update flow:

```text
ProfileScreen
    -> onUpdateProfile(UserProfile)
        -> FinanceViewModel.updateProfile()
            -> UserRepository.upsertProfile()
                -> UserProfileDao.upsert()
                -> Firestore users/{uid}/profile/main
```

Theme mode flow:

```text
Profile Appearance Dialog
    -> selected ThemeMode
        -> saved in UserProfile.themeMode
            -> Room user_profiles.themeMode
            -> Firestore profile document
                -> MainActivity observes profile
                    -> FinanceTrackerTheme applies light/dark/system theme
```

### What Chamika Should Explain In Presentation

- Dashboard uses live Room/Firestore-backed data.
- Dashboard values are calculated from transaction records, not hardcoded.
- Profile settings are saved in Room and synced to Firestore.
- Theme selection is stored per user.
- Profile image is stored as a local URI path.
- Bottom navigation and FAB placement are shared from the main app shell.

## Domain 2: Yesen

Area:

```text
Transactions page
Add income page
Add expense page
Create account page
Related backend logic and DB communication
```

### Main Files

Transactions UI:

```text
ui/screen/transactions/TransactionsScreen.kt
ui/screen/transactions/AddIncomeScreen.kt
ui/screen/transactions/AddExpenseScreen.kt
ui/components/TransactionListItem.kt
ui/components/SpendlyAddActionMenu.kt
ui/components/SpendlyMonthPicker.kt
ui/util/AmountVisualTransformation.kt
ui/util/CategorySettings.kt
ui/util/MonthOptions.kt
ui/util/UiUtils.kt
```

Create Account UI:

```text
ui/screen/CreateAccountScreen.kt
ui/viewmodel/CreateAccountViewModel.kt
```

Transactions backend:

```text
ui/viewmodel/TransactionsViewModel.kt
ui/viewmodel/AddIncomeViewModel.kt
ui/viewmodel/AddExpenseViewModel.kt
data/model/FinanceTransaction.kt
data/repository/TransactionRepository.kt
data/repository/FirebaseTransactionRepository.kt
data/repository/IncomeRepository.kt
data/repository/ExpenseRepository.kt
data/remote/IncomeRepositoryImpl.kt
data/remote/ExpenseRepositoryImpl.kt
data/local/entity/IncomeEntryEntity.kt
data/local/entity/ExpenseEntryEntity.kt
data/local/dao/IncomeDao.kt
data/local/dao/ExpenseDao.kt
data/service/CurrencyRateService.kt
data/service/CryptoRateService.kt
util/Mappers.kt
```

Create Account backend:

```text
data/repository/AuthRepository.kt
data/repository/FirebaseAuthRepository.kt
data/local/entity/UserProfileEntity.kt
data/local/dao/UserProfileDao.kt
```

### Transactions Screen Logic

`TransactionsScreen.kt` shows the History page.

Step-by-step logic:

1. Gets `TransactionsViewModel` using `hiltViewModel()`.
2. Collects `TransactionsUiState`.
3. Shows title "History".
4. Uses `SpendlyMonthPicker` for month selection.
5. Shows filter chips: All, Expenses, Incomes.
6. Filters transactions by selected month and transaction type.
7. Groups transactions by date.
8. Displays each transaction using `TransactionListItem`.
9. Edit action navigates to Add Income or Add Expense based on transaction type.
10. Delete action opens confirmation dialog.
11. Confirm delete calls `TransactionsViewModel.delete()`.

ViewModel filtering:

```text
TransactionsUiState.filtered
    -> filter by tab
    -> filter by selected month
    -> sort by dateMillis descending
```

Grouping:

```text
filtered.groupBy(date label)
```

### Add Income Logic

`AddIncomeScreen.kt` handles income creation and editing.

UI inputs:

- amount
- currency
- income source
- name
- date
- note
- recurring toggle
- crypto coin
- crypto amount
- crypto rate
- exchange rate

Step-by-step save flow:

```text
AddIncomeScreen
    -> AddIncomeViewModel.save()
        -> validate name
        -> validate amount
        -> validate exchange rate if needed
        -> validate crypto rate if crypto
        -> create TransactionDraft
        -> IncomeRepository.addIncome() or updateIncome()
        -> IncomeRepositoryImpl inserts Room entity
        -> IncomeRepositoryImpl writes Firestore document
        -> mark local row synced
```

Currency logic:

- If selected currency equals default currency, exchange rate is not needed.
- If selected currency differs, user can update API rate or manually enter rate.
- Converted default-currency amount is stored in `amountCents`.
- Original amount and original currency are also stored.

Crypto logic:

- User can choose BTC, ETH, USDT, BNB, SOL, XRP, DOGE, or Other.
- For known coins, `CryptoRateService` can update rate.
- For Other, manual rate is required.

Income source settings:

- Default sources are shown.
- User can create custom sources.
- User can hide/delete sources.
- Settings are saved as JSON in the user profile through `categorySettingsJson`.

### Add Expense Logic

`AddExpenseScreen.kt` handles expense creation and editing.

UI inputs:

- amount
- currency
- category
- expense type
- payment method
- name
- date
- note
- exchange rate

Step-by-step save flow:

```text
AddExpenseScreen
    -> AddExpenseViewModel.save()
        -> validate user
        -> validate name
        -> validate amount
        -> validate exchange rate if needed
        -> check available balance
        -> create TransactionDraft
        -> ExpenseRepository.addExpense() or updateExpense()
        -> ExpenseRepositoryImpl inserts Room entity
        -> ExpenseRepositoryImpl writes Firestore document
        -> mark local row synced
```

Category logic:

- Default categories include Food, Transport, Rent, Subscriptions, Entertainment, Gym, Goal, and Other.
- User can create custom categories.
- User can hide default categories or remove custom categories.
- Category settings sync through profile JSON.

Expense type logic:

- Rent, Subscriptions, Gym, and Goal are treated as committed.
- Other categories are treated as discretionary by default.
- User can manually change expense type.

Payment method logic:

- Fixed methods include Card, Cash, and Auto-debit.

### Create Account Logic

`CreateAccountScreen.kt` displays registration form.

Fields:

- name
- email
- password
- confirm password
- currency dropdown

Step-by-step flow:

```text
CreateAccountScreen
    -> CreateAccountViewModel.submit()
        -> validate name
        -> validate email
        -> validate password length
        -> validate confirm password
        -> AuthRepository.createAccount()
            -> FirebaseAuth.createUserWithEmailAndPassword()
            -> create UserProfileEntity
            -> save profile to Room
            -> save profile to Firestore users/{uid}/profile/main
            -> mark profile synced
        -> start immediate WorkManager sync
```

### What Yesen Should Explain In Presentation

- Transactions are combined from separate income and expense tables.
- History is filtered by month and transaction type.
- Add Income and Add Expense save all visible input fields.
- Currency conversion stores converted value as the main amount.
- Original amount/currency are preserved for audit.
- Category/source customization is saved in profile JSON and synced.
- Create account creates Firebase Auth user and initializes profile data.

## Domain 3: Nikini

Area:

```text
Goal page
Login page
Related backend logic and DB communication
```

### Main Files

Login:

```text
ui/screen/AuthScreen.kt
ui/viewmodel/FinanceViewModel.kt
data/repository/AuthRepository.kt
data/repository/FirebaseAuthRepository.kt
data/model/UserSession.kt
```

Goals UI:

```text
ui/screen/goals/GoalsScreen.kt
ui/screen/goals/AddGoalScreen.kt
ui/screen/goals/EditGoalScreen.kt
ui/screen/goals/GoalDetailsScreen.kt
ui/screen/goals/GoalScreen.kt
ui/util/GoalIconUtils.kt
```

Goals backend:

```text
ui/viewmodel/GoalsViewModel.kt
data/model/SavingsGoal.kt
data/repository/GoalRepository.kt
data/remote/GoalRepositoryImpl.kt
data/local/entity/SavingsGoalEntity.kt
data/local/dao/GoalDao.kt
data/repository/TransactionRepository.kt
data/local/entity/ExpenseEntryEntity.kt
data/local/dao/ExpenseDao.kt
```

### Login Logic

`AuthScreen.kt` displays sign-in UI.

Fields and actions:

- email input
- password input
- password visibility toggle
- forgot password
- sign in button
- create account button

Step-by-step login flow:

```text
AuthScreen
    -> FinanceViewModel.submitAuth()
        -> validate email
        -> validate password length
        -> AuthRepository.signIn()
            -> FirebaseAuth.signInWithEmailAndPassword()
        -> syncNow(uid)
        -> WorkManager immediate sync
        -> observe user profile, transactions, goals
        -> navigate into main app automatically
```

Forgot password flow:

```text
Forgot password click
    -> FinanceViewModel.sendPasswordReset()
        -> AuthRepository.sendPasswordResetEmail()
            -> FirebaseAuth sends email
```

Session handling:

`FirebaseAuthRepository.observeSession()` listens to Firebase Auth state and emits `UserSession`.

### Goal Screen Logic

`GoalsScreen.kt` shows the goal tracker.

Step-by-step logic:

1. Reads goals from `FinanceUiState`.
2. Separates achieved goals from active goals.
3. Separates primary goals and other goals.
4. Displays Primary Goals section.
5. Displays Other Goals section.
6. Displays Achieved Goals section if completed goals exist.
7. Add button navigates to Add Goal.
8. Goal click navigates to Goal Details.

### Add Goal Logic

`AddGoalScreen.kt` and `GoalsScreen.kt` goal form logic allow:

- goal name
- status
- target amount
- target date
- initial saved amount
- primary goal switch
- automatic icon suggestion
- manual icon picker

Icon logic:

```text
Goal name contains "car", "vehicle", "bike" -> transport icon
Goal name contains "home", "house", "rent" -> home icon
Goal name contains "travel", "trip", "vacation" -> travel icon
Unknown goal -> default flag icon
```

Save flow:

```text
AddGoalScreen
    -> GoalsViewModel.saveDraft()
        -> validate title
        -> validate target amount
        -> validate target date
        -> validate initial saved amount
        -> create SavingsGoal
        -> GoalRepository.saveGoal()
            -> insert goal into Room
            -> write goal to Firestore
            -> mark goal synced
        -> if initial saved amount exists
            -> create linked Goal expense
```

### Goal Details Logic

Goal details page shows:

- goal title
- target amount
- saved amount
- remaining amount
- progress bar
- status
- monthly savings chart
- required monthly savings
- add savings dialog
- edit action

Add savings flow:

```text
GoalDetailsScreen
    -> GoalsViewModel.addSavings()
        -> parse amount
        -> check amount > 0
        -> check amount does not exceed remaining target
        -> check enough available balance
        -> GoalRepository.addSavings()
            -> update goal savedCents
            -> update status if target reached
            -> save linked expense with category "Goal"
            -> sync goal and expense
```

### Edit Goal Logic

Edit goal supports:

- updating goal name
- status
- target amount
- target date
- primary goal switch
- icon selection
- delete goal

Delete flow:

```text
EditGoalScreen
    -> GoalsViewModel.deleteGoal()
        -> GoalRepository.deleteGoal()
            -> delete goal from Room
            -> delete goal document from Firestore
```

### What Nikini Should Explain In Presentation

- Login uses Firebase Authentication.
- Session state is observed reactively.
- Goals are persisted in Room and synced with Firestore.
- Goal savings also create expense transactions.
- Goal progress is calculated from saved amount and target amount.
- Icon suggestion improves goal UX.
- Validation prevents saved amount exceeding target amount.

## Domain 4: Mahima

Area:

```text
Analytics page
Analytics backend logic
Initial Room DB and Firebase setup
Entities
Firebase rules
Caching logic
Sync logic
```

### Main Files

Analytics:

```text
ui/screen/analytics/AnalyticsScreen.kt
ui/viewmodel/AnalyticsViewModel.kt
ui/components/SpendlyMonthPicker.kt
ui/util/MonthOptions.kt
ui/util/UiUtils.kt
```

Database setup:

```text
data/local/db/SpendlyDatabase.kt
data/local/entity/IncomeEntryEntity.kt
data/local/entity/ExpenseEntryEntity.kt
data/local/entity/SavingsGoalEntity.kt
data/local/entity/UserProfileEntity.kt
data/local/dao/IncomeDao.kt
data/local/dao/ExpenseDao.kt
data/local/dao/GoalDao.kt
data/local/dao/UserProfileDao.kt
di/AppModule.kt
```

Firebase and sync:

```text
data/firebase/FirebaseBootstrap.kt
data/remote/IncomeRepositoryImpl.kt
data/remote/ExpenseRepositoryImpl.kt
data/remote/GoalRepositoryImpl.kt
data/remote/UserRepositoryImpl.kt
worker/SpendlySyncWorker.kt
util/SyncManager.kt
firestore.rules
firestore.indexes.json
```

External API caching:

```text
data/service/CurrencyRateService.kt
data/service/CryptoRateService.kt
```

### Analytics Logic

`AnalyticsScreen.kt` displays finance summaries and charts.

Analytics sections:

- total income
- total expenses
- spending by category donut chart
- committed vs discretionary split
- monthly overview for 5 months
- income sources

Step-by-step analytics flow:

```text
AnalyticsScreen
    -> AnalyticsViewModel
        -> AuthRepository.getCurrentUserId()
        -> TransactionRepository.observeTransactions(uid)
            -> observe income and expenses
            -> combine into one transaction list
        -> AnalyticsUiState calculates selected month data
        -> UI renders charts and summaries
```

Month selection:

```text
SpendlyMonthPicker
    -> AnalyticsViewModel.selectMonth(startMillis)
    -> selectedMonthStartMillis updates
    -> selectedMonthTransactions recalculates
```

Analytics calculations:

- `totalIncome`: selected month income total
- `totalExpense`: selected month expense total
- `spendingByCategory`: expenses grouped by category
- `spendingSplit`: committed vs discretionary expenses
- `monthlyOverview`: income and expense for last 5 months relative to selected month
- `incomeSources`: income grouped by source

Chart implementation:

- Donut chart is drawn with Compose `Canvas`.
- Monthly overview chart is built with Compose layout bars.
- Committed/discretionary uses `LinearProgressIndicator`.

### Initial Room DB Setup

`SpendlyDatabase.kt` registers all Room entities and migrations.

Entities:

```text
IncomeEntryEntity
ExpenseEntryEntity
SavingsGoalEntity
UserProfileEntity
```

DAO setup:

```text
IncomeDao
ExpenseDao
GoalDao
UserProfileDao
```

Hilt database setup:

```text
AppModule.provideDatabase()
    -> Room.databaseBuilder()
    -> addMigrations()
    -> build()
```

### Firebase Setup

Firebase is initialized and checked through:

```text
FirebaseBootstrap.kt
FirebaseAuthRepository.kt
AppModule.kt
```

Firebase services used:

- Firebase Authentication for login/create account/password reset/change password
- Firestore for cloud data storage
- Firestore local persistence for caching

Firestore cache setup:

```text
FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
```

### WorkManager Sync Logic

`SpendlySyncWorker.kt` runs sync when network is connected.

Step-by-step:

```text
1. Worker starts.
2. Gets current Firebase uid.
3. If no uid, finishes successfully.
4. Runs profile, income, expenses, and goal sync.
5. If sync succeeds, returns Result.success().
6. If sync fails, retries up to 3 attempts.
```

`SyncManager.kt` schedules:

- periodic sync
- immediate sync

Periodic sync:

```text
Every 15 minutes
Network required
Unique periodic work name: SpendlyPeriodicSync
```

Immediate sync:

```text
Triggered after sign-in/create-account
Network required
Unique work name: SpendlyImmediateSync
```

### Firestore Rules Note

Current app code writes to:

```text
users/{uid}/profile/main
users/{uid}/income/{incomeId}
users/{uid}/expenses/{expenseId}
users/{uid}/goals/{goalId}
```

Current `firestore.rules` appears to reference an older structure:

```text
users/{userId}/transactions/{transactionId}
```

For project demo or deployment, the Firestore rules should be updated to match the implemented collections.

### What Mahima Should Explain In Presentation

- Room entities define local database schema.
- DAOs provide structured database access.
- Migrations preserve existing user data.
- Firestore stores each user's data under Firebase uid.
- Firestore local persistence provides caching.
- WorkManager sync keeps Room and Firestore connected.
- Analytics uses actual transaction data, not hardcoded values.

## 5. Main Functional Logic To Understand

### Authentication

```text
User enters email/password
FinanceViewModel validates input
FirebaseAuthRepository signs in with Firebase
Session listener emits UserSession
FinanceViewModel syncs profile/transactions/goals
Main app opens
```

### Create Account

```text
User enters registration details
CreateAccountViewModel validates fields
FirebaseAuth creates account
UserProfileEntity is created
Profile saved locally in Room
Profile saved remotely in Firestore
Immediate sync starts
```

### Dashboard

```text
FinanceViewModel observes profile, transactions, and goals
FinanceUiState calculates totals
HomeScreen displays balance, savings, goal, and recent transactions
```

### Add Income

```text
User enters amount/source/date/note
Optional exchange or crypto rate is applied
Converted amount is stored as amountCents
Original amount/currency are also stored
Room insert happens first
Firestore sync happens after local insert
```

### Add Expense

```text
User enters amount/category/type/payment/date/note
Optional exchange rate is applied
Available balance is checked
Room insert happens first
Firestore sync happens after local insert
```

### History

```text
Income and expense streams are combined
Transactions are filtered by selected month
Transactions are filtered by All/Expenses/Incomes tab
Transactions are grouped by date
Rows support expand, edit, and delete
```

### Goals

```text
User creates goal
Goal saved in Room and Firestore
Initial saved amount creates linked expense
Add Savings updates savedCents and creates Goal expense
Progress is calculated from savedCents / targetCents
Goal icon is suggested from goal name
```

### Analytics

```text
Transactions are loaded from repository
Selected month controls analytics scope
Income and expense totals are calculated
Expenses are grouped by category
Committed/discretionary split is calculated
Five-month overview is generated
Charts are rendered with Compose UI and Canvas
```

### Profile

```text
Profile data is loaded from Room
Profile updates are saved locally
Profile updates sync to Firestore
Theme mode changes update app theme
Profile image URI is saved in profile
Password changes use Firebase reauthentication
```

### Sync

```text
Every write sets isSynced = false
Repository tries Firestore write
Successful write sets isSynced = true
WorkManager periodically retries unsynced records
Firestore remote records are pulled into Room
```

## 6. Additional Knowledge For Presentation

### Why Room And Firestore Both?

Room gives fast offline local storage. Firestore gives cloud backup and cross-device synchronization. Using both creates a local-first app where the user can still see and create data even when the network is unreliable.

### Why Hilt?

Hilt manages dependencies like repositories, DAOs, Firebase, and WorkManager. Without Hilt, each screen or ViewModel would need to manually construct dependencies, which creates tightly coupled code.

### Why Repository Pattern?

The repository pattern hides data source details from ViewModels. ViewModels do not need to know whether data comes from Room, Firestore, or an API.

### Why Store Money As Cents?

Money is stored as `Long` cents to avoid floating-point precision errors. For example, LKR 50,000 is stored as `5000000` cents.

### Why Use Firebase UID?

Each user gets a unique Firebase uid. All Firestore documents are stored under that uid, so one user's data does not mix with another user's data.

### Why WorkManager?

WorkManager is reliable for background work. It can run sync later when the network is available and can retry failed sync attempts.

### Why Material 3 And Compose?

Jetpack Compose makes UI declarative. Material 3 provides modern components, theming, typography, and dark mode support.

### Current Strengths

- Clean layered architecture
- Local Room database
- Firestore cloud sync
- Firebase Authentication
- Hilt dependency injection
- WorkManager background sync
- Separate screens and ViewModels
- Reusable UI components
- Theme mode support
- Live analytics from real transaction data
- Goal savings connected to expense records

### Current Limitations / Future Improvements

- Firestore rules should be updated to match the implemented collection structure.
- Delete sync should be improved with soft-delete fields.
- Goal savings should use a more atomic transaction/batch strategy.
- More unit tests and UI tests should be added.
- Room migration tests should be added.
- Error messages should be polished for final user experience.
- Firebase Storage can be added later for cloud profile images.
- Budget limits and recurring transaction automation can be added as future features.

## 7. Suggested Presentation Flow

Use this speaking order:

1. Introduce Spendly and its purpose.
2. Explain the technology stack.
3. Explain MVVM and local-first architecture.
4. Show folder structure.
5. Explain Room database tables.
6. Explain Firestore user-based structure.
7. Explain each member domain.
8. Demo login/create account.
9. Demo dashboard.
10. Demo add income/add expense.
11. Demo history filters.
12. Demo goals and add savings.
13. Demo analytics.
14. Demo profile and theme setting.
15. Explain sync and WorkManager.
16. Mention limitations and future improvements.

## 8. Short Viva Answers

Question: Why did you use MVVM?

Answer: MVVM separates UI from business logic. Compose screens only render state and send events. ViewModels handle validation, state, and repository calls.

Question: Why did you use Room?

Answer: Room provides offline local storage, fast queries, and reactive Flow updates for the UI.

Question: Why did you use Firestore?

Answer: Firestore provides cloud backup and cross-device synchronization for each Firebase user.

Question: How is user data separated?

Answer: All cloud data is stored under `users/{uid}` where `uid` is the Firebase Authentication user id.

Question: How do transactions work?

Answer: Income and expenses are stored in separate Room tables and Firestore subcollections. The transaction repository combines them into one list for history, dashboard, and analytics.

Question: How does sync work?

Answer: Repositories save to Room first, then attempt Firestore sync. WorkManager periodically retries unsynced data when network is available.

Question: How are analytics calculated?

Answer: Analytics observes real transactions, filters them by selected month, then calculates totals, category percentages, committed/discretionary split, income sources, and monthly overview.

Question: How are goals connected with expenses?

Answer: Initial saved amount and add savings create expense records with category `Goal` and a linked `goalId`, while also updating the goal's `savedCents`.

Question: How does dark mode work?

Answer: The profile stores `themeMode` as `SYSTEM`, `LIGHT`, or `DARK`. `MainActivity` reads this value and applies the correct Material 3 color scheme.

Question: What is the main future improvement?

Answer: The most important future improvement is stronger sync conflict handling, especially for deletes and goal-saving batch operations.
