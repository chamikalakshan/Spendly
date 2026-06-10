# Spendly Firebase Security Setup

The repository contains hardened ownership and basic schema validation rules in:

- `firestore.rules`
- `storage.rules`

Deploy them before using a production Firebase project:

```bash
firebase deploy --only firestore:rules,storage
```

## Required Console Configuration

1. Enable Email/Password authentication.
2. Restrict the Android Firebase API key to the Spendly application ID and required Firebase APIs.
3. Enable Firebase App Check for Firestore and Storage.
4. Register the debug App Check token during local development.
5. Use Play Integrity as the production Android App Check provider.
6. Start App Check in monitoring mode, verify legitimate traffic, then enable enforcement.

App Check reduces abuse from unofficial clients, but it does not replace Firestore or Storage security rules.

## Data Ownership

All cloud data is stored below the authenticated Firebase user ID:

```text
users/{uid}/profile/main
users/{uid}/income/{id}
users/{uid}/expenses/{id}
users/{uid}/goals/{id}
users/{uid}/budgets/{id}
users/{uid}/recurringRules/{id}
```

The deployed rules deny cross-user access and reject malformed writes for the supported collections.

## Local Privacy

Android backup is disabled because the Room database contains financial data. Generated reports are stored in the app cache, omit email and transaction notes by default, and are deleted after 24 hours.
