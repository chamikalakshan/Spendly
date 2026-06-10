# Financial Tracker Mobile - Project Overview

## UI Screens

### Main Screens (Modular)
| Screen | Filename | Path |
|--------|----------|------|
| Home | HomeScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/home/HomeScreen.kt` |
| Transactions | TransactionsScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/transactions/TransactionsScreen.kt` |
| Goals | GoalsScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/goals/GoalsScreen.kt` |
| Analytics | AnalyticsScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/analytics/AnalyticsScreen.kt` |
| Profile | ProfileScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/profile/ProfileScreen.kt` |

### Navigation & Auth Screens
| Screen | Filename | Path |
|--------|----------|------|
| Main App (Orchestrator) | MainAppScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/MainAppScreen.kt` |
| Authentication | AuthScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/AuthScreen.kt` |
| Firebase Setup | FirebaseSetupScreen.kt | `app/src/main/kotlin/com/spendly/financetracker/ui/screen/FirebaseSetupScreen.kt` |

---

## Relevant Files

### Reusable Components (Modular)
- **AppBottomNavigation.kt** - Bottom navigation bar with 5 tabs
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/components/AppBottomNavigation.kt`

- **HeaderSection.kt** - Header with greeting and sign out button
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/components/HeaderSection.kt`

- **SummaryCard.kt** - Reusable card for displaying label + amount (Income/Expenses)
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/components/SummaryCard.kt`

- **SummaryPanel.kt** - Complete summary panel (Income, Expenses, Net Savings)
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/components/SummaryPanel.kt`

- **TransactionListItem.kt** - Reusable transaction display card
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/components/TransactionListItem.kt`

- **GoalCard.kt** - Secondary goal card with progress (GoalCard + PrimaryGoalCard)
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/components/GoalCard.kt`

- **ProfileStat.kt** - Reusable stat display component (Goals count, Transactions, Savings %)
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/components/ProfileStat.kt`

### UI & Theme Files
- **FinanceTrackerApp.kt** - Main app composable entry point
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/FinanceTrackerApp.kt`

- **Color.kt** - Color palette and theme colors
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/theme/Color.kt`

- **Theme.kt** - Material 3 theme configuration
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/theme/Theme.kt`

- **Type.kt** - Typography configuration
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/theme/Type.kt`

### ViewModel & State Management
- **FinanceViewModel.kt** - Main view model for managing finance data
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/FinanceViewModel.kt`

- **FinanceUiState.kt** - UI state data classes
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/viewmodel/FinanceUiState.kt`

### Data Models
- **FinanceTransaction.kt** - Transaction data model
  - Path: `app/src/main/kotlin/com/spendly/financetracker/data/model/FinanceTransaction.kt`

- **UserSession.kt** - User session data model
  - Path: `app/src/main/kotlin/com/spendly/financetracker/data/model/UserSession.kt`

### Repositories (Data Access Layer)
- **FirebaseAuthRepository.kt** - Firebase authentication repository implementation
  - Path: `app/src/main/kotlin/com/spendly/financetracker/data/repository/FirebaseAuthRepository.kt`

- **AuthRepository.kt** - Abstract authentication repository interface
  - Path: `app/src/main/kotlin/com/spendly/financetracker/data/repository/AuthRepository.kt`

- **FirebaseTransactionRepository.kt** - Firebase transaction repository implementation
  - Path: `app/src/main/kotlin/com/spendly/financetracker/data/repository/FirebaseTransactionRepository.kt`

- **TransactionRepository.kt** - Abstract transaction repository interface
  - Path: `app/src/main/kotlin/com/spendly/financetracker/data/repository/TransactionRepository.kt`

### Firebase Integration
- **FirebaseBootstrap.kt** - Firebase initialization and setup
  - Path: `app/src/main/kotlin/com/spendly/financetracker/data/firebase/FirebaseBootstrap.kt`

### Application Entry Point
- **MainActivity.kt** - Android activity entry point
  - Path: `app/src/main/kotlin/com/spendly/financetracker/MainActivity.kt`

### Utilities
- **UiUtils.kt** - Utility functions for UI
  - Path: `app/src/main/kotlin/com/spendly/financetracker/ui/util/UiUtils.kt`

### Configuration Files
- **build.gradle.kts** - App module build configuration
  - Path: `app/build.gradle.kts`

- **settings.gradle.kts** - Project settings
  - Path: `settings.gradle.kts`

- **gradle.properties** - Gradle properties
  - Path: `gradle.properties`

- **local.properties** - Local development properties
  - Path: `local.properties`

- **google-services.json** - Google/Firebase services configuration
  - Path: `app/google-services.json`

- **firebase.json** - Firebase configuration
  - Path: `firebase.json`

- **firestore.rules** - Firestore security rules
  - Path: `firestore.rules`

- **firestore.indexes.json** - Firestore index definitions
  - Path: `firestore.indexes.json`

### Documentation
- **README.md** - Project documentation
  - Path: `README.md`

---

## Architecture Summary

**MVVM Pattern (Clean & Modular):**

```
ViewModel (Single Source of Truth)
    ↓
    └─→ State (FinanceUiState) - immutable data
            ↓
            └─→ UI Layer (Composables)
```

**Directory Structure:**
```
ui/
├── screen/
│   ├── MainAppScreen.kt          (Navigation orchestrator)
│   ├── home/
│   │   └── HomeScreen.kt         (Dashboard with recent transactions)
│   ├── transactions/
│   │   └── TransactionsScreen.kt (All transactions with filters)
│   ├── goals/
│   │   └── GoalsScreen.kt        (Primary + secondary goals)
│   ├── analytics/
│   │   └── AnalyticsScreen.kt    (Charts & insights)
│   ├── profile/
│   │   └── ProfileScreen.kt      (User profile & stats)
│   ├── AuthScreen.kt             (Authentication)
│   ├── DashboardScreen.kt        (Legacy - now split to screens)
│   └── FirebaseSetupScreen.kt    (Firebase initialization)
├── components/                    (Reusable UI components)
│   ├── AppBottomNavigation.kt
│   ├── HeaderSection.kt
│   ├── SummaryCard.kt
│   ├── SummaryPanel.kt
│   ├── TransactionListItem.kt
│   ├── GoalCard.kt
│   └── ProfileStat.kt
├── viewmodel/
│   ├── FinanceViewModel.kt       (Manages ALL screens)
│   └── FinanceUiState.kt         (State definitions)
└── theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

**Key Principles:**

1. **One ViewModel** - `FinanceViewModel` manages state for all 5 screens
2. **Stateless Screens** - Each screen receives state and callbacks, no internal state
3. **Reusable Components** - Separated into individual files for maximum reusability
4. **Navigation Orchestrator** - `MainAppScreen` handles navigation logic only
5. **Clear Separation** - UI Layer → ViewModel → Repository → Data Layer

**Layers:**
1. **UI Layer** - Composable screens + reusable components (Jetpack Compose)
2. **ViewModel Layer** - State management with MVVM pattern (no business logic in UI)
3. **Repository Layer** - Abstract data access layer (Auth + Transactions)
4. **Firebase Layer** - Backend services (Authentication, Firestore)
5. **Model Layer** - Data classes representing domain entities

**Technology Stack:**
- Jetpack Compose (UI Framework)
- Firebase Authentication
- Firestore (Database)
- Kotlin Coroutines (Async operations)
- Material Design 3 (Design System)
