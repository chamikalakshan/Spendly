# Spendly Firebase And Firestore Setup

## Firebase Auth

Spendly uses Firebase Authentication email/password accounts. The Firebase `uid` is the only owner key for app data, so the same account can load the same records from any device.

Required Firebase console setup:

1. Open Firebase Console.
2. Select the Spendly project.
3. Go to Authentication > Sign-in method.
4. Enable Email/Password.
5. Add the Android app package used by this project.
6. Download `google-services.json` and place it in `app/google-services.json`.

Implemented auth flows:

- Sign in: `FirebaseAuth.signInWithEmailAndPassword`.
- Create account: `FirebaseAuth.createUserWithEmailAndPassword`.
- Forgot password: `FirebaseAuth.sendPasswordResetEmail`.
- Change password: `FirebaseAuth.currentUser.updatePassword`.

Firebase may reject password changes when the user signed in too long ago. If that happens, the user must sign in again, then retry the password change.

## Firestore Collections

Firestore collections are created automatically when the app writes the first document. You do not need to manually create empty collections.

Spendly writes user data under:

```text
users/{uid}/profile/main
users/{uid}/income/{incomeId}
users/{uid}/expenses/{expenseId}
users/{uid}/goals/{goalId}
```

The app also stores synced profile settings in `users/{uid}/profile/main`, including:

```text
defaultCurrency
profileImageUri
exchangeRateSettings
categorySettingsJson
```

`categorySettingsJson` contains custom and hidden income/expense categories so category changes follow the user across devices.

## Cross-Device Sync

After sign-in or account creation, Spendly immediately syncs Firestore into Room for the current Firebase `uid`. WorkManager also runs background sync when the network is connected.

Sync behavior:

- Local writes save to Room first.
- Successful Firestore writes mark rows as synced.
- Failed Firestore writes stay local with `isSynced=false`.
- Later sync uploads unsynced rows.
- Firestore reads update Room only when the remote record is newer than the local record.

## Indexes

The current queries are simple collection reads under a user document. Firestore creates single-field indexes automatically. Composite indexes are not required unless future queries add multiple `where` and `orderBy` clauses on the same collection.

## Profile Images

Profile pictures are stored as local device URI strings. Firebase Storage is not used in this version, so a profile picture may not display on another device unless that device has access to the same local URI. The rest of the profile data syncs through Firestore.
