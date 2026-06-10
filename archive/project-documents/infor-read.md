# Spendly Member Commit Split And Merge Order

This file explains how to divide the current Spendly work by member/domain and which common files must be present before page-specific branches are merged.

## Team Domains

| Member | Domain |
| --- | --- |
| Mahima | Firebase and Room DB creation/init only, Analytics page and analytics backend logic |
| Yesen | Transactions page and transaction backend logic, Create Account page and related logic |
| Nikini | Goal pages and goal backend logic, Login page and related logic |
| Chamika | Dashboard and Profile pages and related backend logic |

## Important Rule

Do not commit `Sample Guide/`, `.idea/`, `.DS_Store`, generated APK/build files, or unrelated documentation changes into member branches unless the team explicitly agrees.

Repository, DAO, entity, ViewModel, and screen files should go with the member who owns that feature. The shared database shell and dependency wiring should go first so every feature branch can compile.

## Commit And Merge Order

1. Shared foundation commit to `main`
2. Mahima Firebase/Room init commit to `main`
3. Yesen Transactions/Create Account branch
4. Nikini Login/Goals branch
5. Chamika Dashboard/Profile branch
6. Mahima Analytics branch
7. Final integration fix commit if route conflicts or shared ViewModel conflicts remain

This order is safest because all feature branches depend on the shared data models, DI modules, navigation routes, and utility files.

## 1. Shared Foundation Commit

Commit these first into `main` or a shared foundation branch. These files are common dependencies for all members.

```text
app/src/main/kotlin/com/spendly/financetracker/MainActivity.kt
app/src/main/kotlin/com/spendly/financetracker/SpendlyApplication.kt
app/src/main/kotlin/com/spendly/financetracker/ui/FinanceTrackerApp.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/Screen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/BottomNavItem.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/SpendlyBottomNavBar.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/SpendlyNavGraph.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/AppBottomNavigation.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/SpendlyDesign.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/SpendlyAddActionMenu.kt
app/src/main/kotlin/com/spendly/financetracker/ui/theme/Color.kt
app/src/main/kotlin/com/spendly/financetracker/ui/theme/Theme.kt
app/src/main/kotlin/com/spendly/financetracker/ui/theme/Type.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/UiUtils.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/AmountVisualTransformation.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/MonthOptions.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/FinanceUiState.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/FinanceViewModel.kt
app/src/main/kotlin/com/spendly/financetracker/util/Mappers.kt
app/src/main/kotlin/com/spendly/financetracker/util/SyncManager.kt
```

Suggested commit message:

```text
Add shared app foundation and navigation state
```

## 2. Mahima - Firebase And Room DB Init

Mahima owns DB creation/init and shared backend wiring, not every feature's repository logic.

Commit these after the shared foundation:

```text
app/src/main/kotlin/com/spendly/financetracker/data/firebase/FirebaseBootstrap.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/db/SpendlyDatabase.kt
app/src/main/kotlin/com/spendly/financetracker/di/AppModule.kt
app/src/main/kotlin/com/spendly/financetracker/di/RepositoryModule.kt
app/src/main/kotlin/com/spendly/financetracker/worker/SpendlySyncWorker.kt
firebase_&_firestore.md
```

Shared model/entity files that must be present with Mahima's DB init:

```text
app/src/main/kotlin/com/spendly/financetracker/data/model/UserSession.kt
app/src/main/kotlin/com/spendly/financetracker/data/model/UserProfile.kt
app/src/main/kotlin/com/spendly/financetracker/data/model/FinanceTransaction.kt
app/src/main/kotlin/com/spendly/financetracker/data/model/SavingsGoal.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/UserProfileEntity.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/IncomeEntryEntity.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/ExpenseEntryEntity.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/SavingsGoalEntity.kt
```

Suggested commit message:

```text
Initialize Firebase and Room database foundation
```

## 3. Yesen - Transactions And Create Account

Commit these to Yesen's branch after Mahima's foundation is merged.

```text
app/src/main/kotlin/com/spendly/financetracker/data/local/dao/IncomeDao.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/dao/ExpenseDao.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/IncomeRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/ExpenseRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/TransactionRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/FirebaseTransactionRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/remote/IncomeRepositoryImpl.kt
app/src/main/kotlin/com/spendly/financetracker/data/remote/ExpenseRepositoryImpl.kt
app/src/main/kotlin/com/spendly/financetracker/data/service/CurrencyRateService.kt
app/src/main/kotlin/com/spendly/financetracker/data/service/CryptoRateService.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/transactions/TransactionsScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/transactions/AddIncomeScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/transactions/AddExpenseScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/TransactionListItem.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/TransactionsViewModel.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/AddIncomeViewModel.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/AddExpenseViewModel.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/CreateAccountScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/CreateAccountViewModel.kt
```

Shared/common files that Yesen's branch depends on:

```text
app/src/main/kotlin/com/spendly/financetracker/data/model/FinanceTransaction.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/IncomeEntryEntity.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/ExpenseEntryEntity.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/AmountVisualTransformation.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/CategorySettings.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/MonthOptions.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/UiUtils.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/Screen.kt
```

Suggested commit messages:

```text
Implement transaction data flow and screens
Implement create account flow
```

## 4. Nikini - Login And Goals

Commit these to Nikini's branch after the shared foundation and DB init are merged.

```text
app/src/main/kotlin/com/spendly/financetracker/data/local/dao/GoalDao.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/AuthRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/FirebaseAuthRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/GoalRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/remote/GoalRepositoryImpl.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/AuthScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/SplashScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/goals/GoalScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/goals/AddGoalScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/goals/GoalDetailsScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/goals/EditGoalScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/goals/GoalsScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/GoalIconUtils.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/GoalsViewModel.kt
```

Shared/common files that Nikini's branch depends on:

```text
app/src/main/kotlin/com/spendly/financetracker/data/model/SavingsGoal.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/SavingsGoalEntity.kt
app/src/main/kotlin/com/spendly/financetracker/data/model/UserSession.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/Screen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/UiUtils.kt
app/src/main/kotlin/com/spendly/financetracker/ui/FinanceTrackerApp.kt
```

Suggested commit messages:

```text
Implement login and auth recovery flow
Implement goal tracker data flow and icon selection
```

## 5. Chamika - Dashboard And Profile

Commit these to Chamika's branch after Transactions and Goals are available on `main`, because dashboard/profile display transaction and goal data.

```text
app/src/main/kotlin/com/spendly/financetracker/data/local/dao/UserProfileDao.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/UserRepository.kt
app/src/main/kotlin/com/spendly/financetracker/data/remote/UserRepositoryImpl.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/home/HomeScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/DashboardScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/screen/profile/ProfileScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/HeaderSection.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/SummaryCard.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/SummaryPanel.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/GoalCard.kt
app/src/main/kotlin/com/spendly/financetracker/ui/components/ProfileStat.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/HomeViewModel.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/ProfileViewModel.kt
```

Shared/common files that Chamika's branch depends on:

```text
app/src/main/kotlin/com/spendly/financetracker/data/model/UserProfile.kt
app/src/main/kotlin/com/spendly/financetracker/data/local/entity/UserProfileEntity.kt
app/src/main/kotlin/com/spendly/financetracker/data/model/FinanceTransaction.kt
app/src/main/kotlin/com/spendly/financetracker/data/model/SavingsGoal.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/UiUtils.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/GoalIconUtils.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/Screen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/FinanceTrackerApp.kt
```

Suggested commit messages:

```text
Integrate dashboard with live finance data
Integrate profile settings and account actions
```

## 6. Mahima - Analytics

Commit Analytics after Transactions are merged, because analytics depends on real income/expense records.

```text
app/src/main/kotlin/com/spendly/financetracker/ui/screen/analytics/AnalyticsScreen.kt
app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/AnalyticsViewModel.kt
```

Shared/common files that Mahima's Analytics branch depends on:

```text
app/src/main/kotlin/com/spendly/financetracker/data/model/FinanceTransaction.kt
app/src/main/kotlin/com/spendly/financetracker/data/model/UserSession.kt
app/src/main/kotlin/com/spendly/financetracker/data/repository/TransactionRepository.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/MonthOptions.kt
app/src/main/kotlin/com/spendly/financetracker/ui/util/UiUtils.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/Screen.kt
```

Suggested commit message:

```text
Implement analytics from live transaction data
```

## Final Integration Commit

Use one final small commit only if needed for conflicts between feature branches.

Common candidates:

```text
app/src/main/kotlin/com/spendly/financetracker/ui/FinanceTrackerApp.kt
app/src/main/kotlin/com/spendly/financetracker/ui/navigation/Screen.kt
app/src/main/kotlin/com/spendly/financetracker/di/AppModule.kt
app/src/main/kotlin/com/spendly/financetracker/di/RepositoryModule.kt
app/src/main/kotlin/com/spendly/financetracker/util/Mappers.kt
```

Suggested commit message:

```text
Resolve Spendly feature integration wiring
```

## Recommended Merge Sequence

```text
main
  <- shared foundation
  <- Mahima/Firebase
  <- Yesen/Transactions
  <- Yesen/CreateAccount if separate
  <- Nikini/Login
  <- Nikini/Goals
  <- Chamika/Profile
  <- Chamika/Dashboard
  <- Mahima/Analytics
  <- final integration fixes
```

## Verification After Each Merge

Run:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home sh gradlew :app:compileDebugKotlin
```

Run this after the final merge:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home sh gradlew :app:assembleDebug
```

