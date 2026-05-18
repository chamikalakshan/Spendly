# Spendly

Spendly is an Android personal finance app for tracking income, expenses, transactions, analytics, and savings goals. It uses an offline-first architecture so users can keep recording money activity locally and sync to Firebase when the network is available.

Package name: `com.spendly.app`

## Tech Stack

- Kotlin and Jetpack Compose
- Material Design 3
- Navigation Compose
- Firebase Authentication and Cloud Firestore
- Room database with KSP
- Hilt dependency injection
- Kotlin Coroutines and Flow
- WorkManager background sync
- YCharts for charts

## Firebase Setup

1. Create a Firebase project in the Firebase Console.
2. Add an Android app with package name `com.spendly.app`.
3. Download `google-services.json`.
4. Place `google-services.json` inside the `app/` folder.
5. Enable Email/Password sign-in in Firebase Authentication.
6. Create a Cloud Firestore database and publish the rules from `firestore.rules`.

## Build And Run

1. Open the project in Android Studio.
2. Sync Gradle.
3. Confirm `app/google-services.json` exists.
4. Select an emulator or physical Android device.
5. Run the app from Android Studio, or build from terminal:

```bash
./gradlew assembleDebug
```

## Architecture

```text
Compose Screens
      |
      v
ViewModels (StateFlow)
      |
      v
Repositories
      |
      +--> Room DAOs and Entities
      |
      +--> Firebase Auth and Firestore
      |
      v
WorkManager Sync Worker
```

## Screens

- Splash: routes logged-in users to Dashboard and guests to Login.
- Login: signs in existing users with Firebase Authentication.
- Register: creates a Firebase account and user profile.
- Dashboard: shows monthly income, expenses, savings, goal progress, and recent transactions.
- Add Income: records income sources such as salary, freelance, AdSense, and crypto.
- Add Expense: records expenses with category, type, payment method, date, and note.
- Transactions: lists income and expenses with filters and grouped date headers.
- Analytics: shows spending, income source, and monthly overview charts.
- Goal Tracker: lists the primary savings goal and other goals.
- Primary Goal: shows detailed progress and projection for the main goal.
- Edit Goal: creates, updates, or deletes savings goals.
- Profile: shows user information, preferences, and logout.
