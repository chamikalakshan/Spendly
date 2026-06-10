# Spendly — Smart Personal Finance Management System

PowerPoint slide content plan for a 15-minute live demo and engineering discussion.

Sources used:

- `/Users/chamikalakshan/Downloads/spendly_slide_content_fixed.md`
- `/Users/chamikalakshan/Downloads/FlowLedger-Tech.pdf` as visual/style reference
- `docs/Project_Demo_Preparation_Guide.md`
- `docs/Spendly_SRS.md`
- `firebase_&_firestore.md`
- `infor-read.md`
- Current Kotlin Android codebase under `app/src/main/kotlin/com/spendly/financetracker`

Deck length: 24 slides.

---

## 1. Title Slide

**Slide content**

- Spendly — Smart Personal Finance Management System
- Kotlin Android Financial Tracker App
- MVVM + Room + Firebase + WorkManager
- Built for scenario-driven personal finance tracking

**Suggested visual / diagram idea**

Modern teal title slide with a phone mockup showing a finance dashboard, small technology chips, and a clean app-system line.

**Speaker notes**

Introduce Spendly as a smart Android finance tracker built to help users manage income, expenses, goals, and financial insights. Mention that the presentation supports a live demo and focuses on engineering decisions, not only UI features.

**Possible lecturer question and answer**

Q: Is this only a UI prototype?  
A: No. The app includes Firebase Authentication, Firestore cloud storage, Room local database, repositories, ViewModels, Hilt, WorkManager sync, and live data-driven screens.

---

## 2. Requirement Derivation Process

**Slide content**

- Scenario and user pain points
- Existing tool failure analysis
- Functional requirement mapping
- Non-functional requirement mapping
- Implementation and validation

**Suggested visual / diagram idea**

Left-to-right process timeline: Persona → Problems → Requirements → Architecture → Demo validation.

**Speaker notes**

Explain that requirements were derived from the user scenario, not invented as generic finance-app features. The team identified pain points, mapped them to features, and then mapped features into MVVM, Room, Firestore, and sync design.

**Possible lecturer question and answer**

Q: How did you derive your requirements?  
A: We started from the Kavindu finance scenario, identified manual tracking, fragmented income, poor spending awareness, and savings-goal problems, then mapped each issue into functional and non-functional requirements.

---

## 3. Problem Background & User Persona

**Slide content**

- Kavindu Silva, 25, junior software engineer in Colombo
- Income from salary, freelance, AdSense, and crypto
- Expenses spread across cash, card, rent, food, transport, gym, subscriptions
- Goal: buy MacBook Pro M4
- Core pain: “I cannot explain where my money goes”

**Suggested visual / diagram idea**

Persona card with income streams on one side and expense channels on the other, connected to a central “low visibility” problem.

**Speaker notes**

Use the persona to show why the system needs more than simple income/expense entry. Kavindu has irregular and multi-source income, spending across channels, and a long-term goal that needs regular tracking.

**Possible lecturer question and answer**

Q: Why did this persona influence your data model?  
A: Because the app needs income sources, expense categories, payment methods, currency/rate fields, and goal progress fields to represent Kavindu’s real financial behavior.

---

## 4. Functional Requirements

**Slide content**

- Register, login, logout, reset/change password
- Add/edit/delete income and expenses
- Filter and group transaction history
- Dashboard totals and recent activity
- Create/edit goals and add savings
- Analytics by month, category, type, and source
- Room + Firestore persistence

**Suggested visual / diagram idea**

Compact requirement matrix with four columns: Auth, Transactions, Goals, Insights.

**Speaker notes**

Keep this slide high-level. Mention that the SRS has full IDs, while this deck shows the main requirements that matter for the demo and discussion.

**Possible lecturer question and answer**

Q: Which requirement is most technically important?  
A: Local-first transaction persistence, because it connects UI, ViewModel validation, repository logic, Room, Firestore, and WorkManager sync.

---

## 5. Non-Functional Requirements

**Slide content**

- Performance: Room-backed local loading
- Security: Firebase UID-based ownership
- Reliability: unsynced local records retained
- Maintainability: MVVM + repositories
- Usability: quick entry, consistent UI
- Data integrity: money stored as cents

**Suggested visual / diagram idea**

Quality attribute wheel around Spendly core.

**Speaker notes**

Explain that finance apps are judged by trust. The app must be responsive, secure, reliable, and maintainable. Also mention that storing money as `Long` cents avoids floating-point errors.

**Possible lecturer question and answer**

Q: Why store money as cents?  
A: To avoid floating-point precision errors. The database stores values like `amountCents`, `targetCents`, and `savedCents` as `Long`.

---

## 6. Core User Flows

**Slide content**

- Register or login
- Add income / add expense
- View dashboard update
- Review History filters
- Create goal and add savings
- View Analytics
- Update Profile settings

**Suggested visual / diagram idea**

Flow diagram from Auth → Dashboard → Add Transaction → History/Analytics/Goals/Profile.

**Speaker notes**

Use this slide to preview the live demo order. Emphasize that each user flow is backed by the architecture shown later.

**Possible lecturer question and answer**

Q: What happens after login?  
A: `FinanceViewModel` observes the Firebase session, starts immediate sync, and observes profile, transactions, and goals for the current UID.

---

## 7. Technology Stack

**Slide content**

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| State | Flow / StateFlow |
| Architecture | MVVM + Repository |
| Local DB | Room |
| Cloud/Auth | Firebase Auth + Firestore |
| DI/Sync | Hilt + WorkManager |

**Suggested visual / diagram idea**

Stacked technology tower with mobile UI on top and Firebase/Room at the bottom.

**Speaker notes**

Mention that these are not decorative choices. Each technology solves a specific project requirement: Compose for UI, StateFlow for reactive state, Room for local persistence, Firestore for cloud sync, Hilt for dependency injection, and WorkManager for background retry.

**Possible lecturer question and answer**

Q: Why use both Room and Firestore?  
A: Room gives fast local loading and offline persistence; Firestore gives cross-device cloud storage.

---

## 8. High-Level Architecture

**Slide content**

- Single Android app
- Compose screens
- ViewModels
- Repository interfaces
- Room database
- Firebase Auth and Firestore
- WorkManager sync

**Suggested visual / diagram idea**

Architecture block diagram: UI → ViewModel → Repository → Room + Firestore, with WorkManager syncing repositories.

**Speaker notes**

Explain the main dependency direction. UI does not directly access Firebase or Room. The ViewModel owns state. Repositories coordinate local and remote data sources.

**Possible lecturer question and answer**

Q: Where is navigation handled?  
A: In `FinanceTrackerApp.kt` using Navigation Compose routes and a shared scaffold/bottom navigation.

---

## 9. MVVM + Repository Architecture

**Slide content**

- UI renders state and sends events
- ViewModel validates and prepares state
- Repository hides data-source details
- DAOs query Room
- Firestore stores remote copy

**Suggested visual / diagram idea**

Layered MVVM diagram with responsibility labels beside each layer.

**Speaker notes**

Give an example: Add Income screen sends Save to `AddIncomeViewModel`; ViewModel validates and calls `IncomeRepository`; repository saves to Room and uploads to Firestore.

**Possible lecturer question and answer**

Q: Why use repository interfaces?  
A: They keep ViewModels independent from concrete Room/Firebase implementations and make the code easier to test and maintain.

---

## 10. Local-First Data Flow

**Slide content**

1. User saves record
2. ViewModel validates
3. Repository writes Room first
4. Firestore upload attempted
5. Success marks `isSynced = true`
6. WorkManager retries failures
7. Flow updates UI

**Suggested visual / diagram idea**

Sequence diagram for adding income or expense.

**Speaker notes**

Stress reliability. The user should not lose data because of weak network. Room becomes the local source of truth for the UI, while Firestore sync happens immediately or later.

**Possible lecturer question and answer**

Q: How does the UI update after saving locally?  
A: DAOs expose Flow streams. ViewModels collect repository flows and expose StateFlow, so Compose recomposes automatically.

---

## 11. ViewModel Structure

**Slide content**

- `FinanceViewModel`: session, dashboard, profile, sync
- `CreateAccountViewModel`: registration validation
- `TransactionsViewModel`: filters, grouping, delete
- `AddIncomeViewModel`: income form + rate logic
- `AddExpenseViewModel`: expense form + balance validation
- `GoalsViewModel`: goals, savings, validation
- `AnalyticsViewModel`: aggregation and chart data

**Suggested visual / diagram idea**

ViewModel map grouped by screen/domain.

**Speaker notes**

Explain that business logic is distributed to screen-specific ViewModels. Analytics calculations, transaction grouping, form validation, and goal validation are not placed directly in Composables.

**Possible lecturer question and answer**

Q: Which ViewModel handles dashboard state?  
A: `FinanceViewModel` combines profile, transactions, and goals into `FinanceUiState` for dashboard/profile-related state.

---

## 12. Room Database Schema

**Slide content**

Entities:

- `UserProfileEntity`
- `IncomeEntryEntity`
- `ExpenseEntryEntity`
- `SavingsGoalEntity`

DAOs:

- `UserProfileDao`
- `IncomeDao`
- `ExpenseDao`
- `GoalDao`

Database: `SpendlyDatabase`, version 5

**Suggested visual / diagram idea**

Mini ER diagram: UserProfile owns Income, Expense, Goal; Expense can link to Goal by `goalId`.

**Speaker notes**

Mention important fields: `amountCents`, `dateMillis`, `updatedAtMillis`, `isSynced`, `goalId`, `themeMode`, and `categorySettingsJson`. Also mention non-destructive migrations up to version 5.

**Possible lecturer question and answer**

Q: What does `isSynced` do?  
A: It marks whether a local row has been successfully written to Firestore.

---

## 13. Firestore Structure & Queries

**Slide content**

```text
users/{uid}/profile/main
users/{uid}/income/{incomeId}
users/{uid}/expenses/{expenseId}
users/{uid}/goals/{goalId}
```

- UID isolates each user’s records
- Repositories write expanded document maps
- Sync pulls remote docs and compares `updatedAtMillis`

**Suggested visual / diagram idea**

Firestore tree diagram with user UID root and four branches.

**Speaker notes**

Explain that Firestore collections are created automatically when documents are written. Also be honest: the repository uses these paths, while the current `firestore.rules` file appears older and should be updated to fully match the current schema.

**Possible lecturer question and answer**

Q: Do we manually create Firestore collections?  
A: No. Firestore creates collections when the app writes the first document.

---

## 14. Sync Logic with WorkManager

**Slide content**

- `SpendlySyncWorker`
- `SyncManager`
- Network constraint: connected
- Periodic sync: 15 minutes
- Immediate sync after sign-in/create/write flows
- Syncs profile, income, expenses, goals

**Suggested visual / diagram idea**

Background sync loop: Unsynced Room rows → Worker → Repositories → Firestore → mark synced.

**Speaker notes**

Explain that WorkManager is used because sync should be reliable and constraint-aware. The worker runs repository sync functions and retries failures up to configured attempts.

**Possible lecturer question and answer**

Q: Why not just sync from the screen?  
A: Screens can trigger immediate writes, but WorkManager handles delayed retry when network becomes available.

---

## 15. Chamika Domain — Dashboard & Profile

**Slide content**

- Dashboard summary and recent transactions
- Profile view and settings
- Default currency and theme mode
- Password/profile actions
- Related backend: User repository + profile DAO

**Suggested visual / diagram idea**

Two-column module card: Dashboard and Profile with data dependencies below.

**Speaker notes**

Explain that Dashboard uses profile, transactions, and goals from `FinanceUiState`. Profile updates go through `FinanceViewModel` to `UserRepositoryImpl`, then Room and Firestore.

**Possible lecturer question and answer**

Q: Does Dashboard query Firestore directly?  
A: No. Dashboard reads ViewModel state derived from repository flows.

---

## 16. Yesen Domain — Transactions & Create Account

**Slide content**

- Create account form and validation
- Add/edit income
- Add/edit expense
- History filters and grouped rows
- Income/expense repositories and DAOs
- Currency and category/source settings

**Suggested visual / diagram idea**

Transaction pipeline: Form → ViewModel → Repository → Room/Firestore → History/Dashboard/Analytics.

**Speaker notes**

Mention that income and expense are separate Room tables and Firestore subcollections, but they are combined into `FinanceTransaction` rows for display.

**Possible lecturer question and answer**

Q: Why separate income and expense tables?  
A: They have different fields and behavior, but are combined through `TransactionRepository` for UI lists and analytics.

---

## 17. Nikini Domain — Login & Goals

**Slide content**

- Login and session handling
- Goal creation/edit/delete
- Icon suggestion and manual icon selection
- Progress and remaining amount calculation
- Add savings with target validation
- Goal saving creates linked `Goal` expense

**Suggested visual / diagram idea**

Goal progress card connected to a History expense row through `goalId`.

**Speaker notes**

Highlight the important logic: adding money to a goal increases `savedCents` and creates an expense transaction with category `Goal`, so financial summaries remain consistent.

**Possible lecturer question and answer**

Q: Why create an expense when adding goal savings?  
A: It records money allocated away from available balance and connects goal progress with transaction history.

---

## 18. Mahima Domain — Analytics, DB Setup & Sync

**Slide content**

- Analytics UI and ViewModel aggregation
- Room database foundation and migrations
- Entities and DAOs
- Firebase/Firestore setup notes
- WorkManager sync and caching logic

**Suggested visual / diagram idea**

Analytics card preview beside a database/sync foundation strip.

**Speaker notes**

Explain that Analytics is calculated from real transaction data. `AnalyticsViewModel` filters by selected month and prepares totals, category percentages, committed/discretionary split, income sources, and 5-month overview.

**Possible lecturer question and answer**

Q: Are analytics hardcoded?  
A: No. They are derived from transactions observed through `TransactionRepository`.

---

## 19. Main Feature Logic to Understand

**Slide content**

- Auth flow: Firebase session → app state
- Transaction flow: validate → Room → Firestore
- Dashboard: sums by `dateMillis`
- Goals: `savedCents / targetCents`
- Analytics: grouped transaction aggregation
- Profile: settings sync through profile document

**Suggested visual / diagram idea**

Six small logic cards with one formula or key line each.

**Speaker notes**

This is a quick viva preparation slide. Use it to show the panel the logic you are ready to explain in code.

**Possible lecturer question and answer**

Q: Which date field is used for monthly reports?  
A: `dateMillis`, because it is the user-selected transaction event date. `createdAtMillis` is only audit metadata.

---

## 20. UI/UX Design Decisions

**Slide content**

- Material 3 Compose UI
- Green/teal fintech theme
- Shared bottom navigation
- Shared floating plus action
- Shared month picker
- Dashboard-first information design
- Dark/light/system appearance support

**Suggested visual / diagram idea**

Design system board: colors, components, navigation, and key screens.

**Speaker notes**

Explain that the UI is designed for repeated daily use. The app avoids long explanatory text in screens and uses cards, chips, forms, and quick actions to reduce friction.

**Possible lecturer question and answer**

Q: Why keep quick-add actions visible?  
A: Transaction entry must be low-friction, otherwise users abandon finance trackers.

---

## 21. Live Demo Flow

**Slide content**

0-1 min: intro  
1-3 min: architecture  
3-5 min: login/register  
5-7 min: dashboard  
7-9 min: transactions  
9-11 min: goals  
11-13 min: analytics/profile  
13-15 min: database/sync Q&A

**Suggested visual / diagram idea**

15-minute timeline with screen icons and technical checkpoints.

**Speaker notes**

Use this as the demo plan. The presentation should be paused here before the live app demo if time is short.

**Possible lecturer question and answer**

Q: What data should be prepared before demo?  
A: At least one income, several categorized expenses, one goal with savings, and transactions in the selected analytics month.

---

## 22. Engineering Decisions & Justifications

**Slide content**

| Decision | Reason |
|---|---|
| MVVM | Separation of UI and business logic |
| Room + Firestore | Offline cache + cloud sync |
| UID-based paths | User data isolation |
| Money as cents | Data accuracy |
| WorkManager | Reliable sync retry |
| Hilt | Dependency wiring |

**Suggested visual / diagram idea**

Decision table with icons and short rationale.

**Speaker notes**

This slide helps answer “why” questions. Emphasize tradeoffs: the team chose practical, maintainable patterns rather than putting everything in screens.

**Possible lecturer question and answer**

Q: Why use Hilt?  
A: It provides Firebase, Room, DAOs, repositories, ViewModels, and workers without manual factories.

---

## 23. Limitations & Future Improvements

**Slide content**

Current limitations:

- Firestore rules need schema update
- Profile image is local URI only
- Offline delete conflict handling can improve
- Dedicated budgets/accounts not implemented

Future:

- Budget alerts
- PDF/CSV exports
- Better sync conflict resolution
- Notifications
- Advanced analytics

**Suggested visual / diagram idea**

Two-column “Now / Next” roadmap.

**Speaker notes**

Be honest and professional. Limitations do not weaken the project if they are clearly understood and realistic future work is proposed.

**Possible lecturer question and answer**

Q: What is the biggest technical improvement needed?  
A: Updating Firestore rules for the current schema and improving offline delete conflict handling.

---

## 24. Conclusion / Q&A

**Slide content**

- Spendly connects daily tracking with financial awareness
- Local-first architecture improves reliability
- MVVM keeps the code explainable and maintainable
- Room + Firestore supports offline and cross-device data
- Ready for live demo and engineering questions

**Suggested visual / diagram idea**

Clean closing slide with the app name, architecture mini-map, and Q&A prompt.

**Speaker notes**

Close by restating the value: the app turns fragmented income, expenses, and goals into one consistent system. Invite questions about architecture, database, sync, and feature logic.

**Possible lecturer question and answer**

Q: What is the strongest technical point of your project?  
A: The local-first MVVM architecture using Room, Firestore, repositories, StateFlow, Hilt, and WorkManager sync.

