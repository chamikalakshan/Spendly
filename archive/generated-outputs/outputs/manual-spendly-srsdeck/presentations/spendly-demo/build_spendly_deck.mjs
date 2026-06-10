import fs from "node:fs/promises";
import path from "node:path";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

const W = 1280;
const H = 720;

const OUT_DIR = "/Users/chamikalakshan/Documents/Codex/Financial-Tracker-Mobile-Kotlin/docs";
const WORKSPACE = "/Users/chamikalakshan/Documents/Codex/Financial-Tracker-Mobile-Kotlin/outputs/manual-spendly-srsdeck/presentations/spendly-demo";
const PREVIEW_DIR = `${WORKSPACE}/preview`;
const LAYOUT_DIR = `${WORKSPACE}/layout`;
const OUTPUT_PPTX = `${OUT_DIR}/Spendly_Smart_Personal_Finance_Management_System.pptx`;

const C = {
  green: "#14A374",
  greenDark: "#087B5B",
  teal: "#0E8F85",
  mint: "#DFF7EE",
  mint2: "#EAFBF5",
  navy: "#10212B",
  ink: "#17212B",
  muted: "#64727B",
  line: "#D9E7E2",
  bg: "#F7FBF9",
  white: "#FFFFFF",
  red: "#E84D5B",
  amber: "#F6A609",
  purple: "#7B61FF",
  blue: "#3B82F6",
};

const slides = [
  {
    title: "Spendly — Smart Personal Finance Management System",
    kicker: "Kotlin Android Financial Tracker App",
    bullets: ["MVVM + Room + Firebase + WorkManager", "Scenario-driven personal finance tracking", "Built for a 15-minute live engineering demo"],
    visual: "title",
    note: "Introduce Spendly as a smart Android finance tracker built to help users manage income, expenses, goals, and financial insights. Mention that the presentation supports a live demo and focuses on engineering decisions, not only UI features.",
    qa: "Q: Is this only a UI prototype? A: No. The app includes Firebase Authentication, Firestore cloud storage, Room local database, repositories, ViewModels, Hilt, WorkManager sync, and live data-driven screens.",
  },
  {
    title: "Requirement Derivation Process",
    bullets: ["Scenario and user pain points", "Existing tool failure analysis", "Functional and quality requirement mapping", "Implementation and validation"],
    visual: "timeline",
    labels: ["Persona", "Problems", "Requirements", "Architecture", "Validation"],
    note: "Explain that requirements were derived from the user scenario, not invented as generic finance-app features. The team identified pain points, mapped them to features, and then mapped features into MVVM, Room, Firestore, and sync design.",
    qa: "Q: How did you derive your requirements? A: We started from the Kavindu finance scenario, identified manual tracking, fragmented income, poor spending awareness, and savings-goal problems, then mapped each issue into functional and non-functional requirements.",
  },
  {
    title: "Problem Background & User Persona",
    bullets: ["Kavindu Silva, 25, junior software engineer in Colombo", "Income from salary, freelance, AdSense, and crypto", "Expenses across cash, card, rent, food, transport, gym, subscriptions", "Core pain: cannot explain where money goes"],
    visual: "persona",
    note: "Use the persona to show why the system needs more than simple income/expense entry. Kavindu has irregular and multi-source income, spending across channels, and a long-term goal that needs regular tracking.",
    qa: "Q: Why did this persona influence your data model? A: Because the app needs income sources, expense categories, payment methods, currency/rate fields, and goal progress fields to represent Kavindu's real financial behavior.",
  },
  {
    title: "Functional Requirements",
    bullets: ["Register, login, logout, reset/change password", "Add/edit/delete income and expenses", "Filter and group transaction history", "Dashboard totals and recent activity", "Create/edit goals and add savings", "Analytics by month, category, type, and source"],
    visual: "matrix",
    matrix: [["Auth", "Register, login, password"], ["Transactions", "Income, expense, history"], ["Goals", "Create, edit, add savings"], ["Insights", "Dashboard, analytics"]],
    note: "Keep this slide high-level. Mention that the SRS has full IDs, while this deck shows the main requirements that matter for the demo and discussion.",
    qa: "Q: Which requirement is most technically important? A: Local-first transaction persistence, because it connects UI, ViewModel validation, repository logic, Room, Firestore, and WorkManager sync.",
  },
  {
    title: "Non-Functional Requirements",
    bullets: ["Performance: Room-backed local loading", "Security: Firebase UID-based ownership", "Reliability: unsynced local records retained", "Maintainability: MVVM + repositories", "Usability: quick entry, consistent UI", "Data integrity: money stored as cents"],
    visual: "wheel",
    labels: ["Performance", "Security", "Reliability", "Maintainability", "Usability", "Integrity"],
    note: "Explain that finance apps are judged by trust. The app must be responsive, secure, reliable, and maintainable. Also mention that storing money as Long cents avoids floating-point errors.",
    qa: "Q: Why store money as cents? A: To avoid floating-point precision errors. The database stores values like amountCents, targetCents, and savedCents as Long.",
  },
  {
    title: "Core User Flows",
    bullets: ["Register or login", "Add income / add expense", "View dashboard update", "Review History filters", "Create goal and add savings", "View Analytics and Profile settings"],
    visual: "flow",
    labels: ["Auth", "Dashboard", "Add Record", "History", "Goals", "Analytics", "Profile"],
    note: "Use this slide to preview the live demo order. Emphasize that each user flow is backed by the architecture shown later.",
    qa: "Q: What happens after login? A: FinanceViewModel observes the Firebase session, starts immediate sync, and observes profile, transactions, and goals for the current UID.",
  },
  {
    title: "Technology Stack",
    bullets: ["Kotlin, Jetpack Compose, Material 3", "MVVM, Repository Pattern, ViewModels", "Flow / StateFlow for reactive updates", "Room local DB + Firebase Auth/Firestore", "Hilt dependency injection + WorkManager sync"],
    visual: "stack",
    labels: ["Compose UI", "ViewModels", "Repositories", "Room DB", "Firebase", "WorkManager"],
    note: "Mention that these are not decorative choices. Each technology solves a specific project requirement: Compose for UI, StateFlow for reactive state, Room for local persistence, Firestore for cloud sync, Hilt for dependency injection, and WorkManager for background retry.",
    qa: "Q: Why use both Room and Firestore? A: Room gives fast local loading and offline persistence; Firestore gives cross-device cloud storage.",
  },
  {
    title: "High-Level Architecture",
    bullets: ["Single Android app", "Compose screens and shared components", "ViewModels expose StateFlow", "Repositories coordinate data operations", "Room and Firestore store user data", "WorkManager retries sync"],
    visual: "architecture",
    note: "Explain the main dependency direction. UI does not directly access Firebase or Room. The ViewModel owns state. Repositories coordinate local and remote data sources.",
    qa: "Q: Where is navigation handled? A: In FinanceTrackerApp.kt using Navigation Compose routes and a shared scaffold/bottom navigation.",
  },
  {
    title: "MVVM + Repository Architecture",
    bullets: ["UI renders state and sends events", "ViewModel validates and prepares state", "Repository hides data-source details", "DAOs query Room", "Firestore stores remote copy"],
    visual: "layers",
    labels: ["Compose UI", "ViewModel", "Repository", "Room DAO", "Firestore"],
    note: "Give an example: Add Income screen sends Save to AddIncomeViewModel; ViewModel validates and calls IncomeRepository; repository saves to Room and uploads to Firestore.",
    qa: "Q: Why use repository interfaces? A: They keep ViewModels independent from concrete Room/Firebase implementations and make the code easier to test and maintain.",
  },
  {
    title: "Local-First Data Flow",
    bullets: ["User saves record", "ViewModel validates", "Repository writes Room first", "Firestore upload attempted", "Success marks isSynced = true", "WorkManager retries failures", "Flow updates UI"],
    visual: "sequence",
    labels: ["Save", "Validate", "Room", "Firestore", "Sync", "StateFlow", "UI"],
    note: "Stress reliability. The user should not lose data because of weak network. Room becomes the local source of truth for the UI, while Firestore sync happens immediately or later.",
    qa: "Q: How does the UI update after saving locally? A: DAOs expose Flow streams. ViewModels collect repository flows and expose StateFlow, so Compose recomposes automatically.",
  },
  {
    title: "ViewModel Structure",
    bullets: ["FinanceViewModel: session, dashboard, profile, sync", "CreateAccountViewModel: registration validation", "TransactionsViewModel: filters, grouping, delete", "AddIncomeViewModel: income form + rate logic", "AddExpenseViewModel: expense form + balance validation", "GoalsViewModel and AnalyticsViewModel: domain logic"],
    visual: "cards",
    labels: ["Finance", "Create Account", "Transactions", "Add Income", "Add Expense", "Goals", "Analytics"],
    note: "Explain that business logic is distributed to screen-specific ViewModels. Analytics calculations, transaction grouping, form validation, and goal validation are not placed directly in Composables.",
    qa: "Q: Which ViewModel handles dashboard state? A: FinanceViewModel combines profile, transactions, and goals into FinanceUiState for dashboard/profile-related state.",
  },
  {
    title: "Room Database Schema",
    bullets: ["Entities: UserProfile, IncomeEntry, ExpenseEntry, SavingsGoal", "DAOs: UserProfileDao, IncomeDao, ExpenseDao, GoalDao", "Database: SpendlyDatabase version 5", "Key fields: amountCents, dateMillis, updatedAtMillis, isSynced, goalId"],
    visual: "er",
    note: "Mention important fields: amountCents, dateMillis, updatedAtMillis, isSynced, goalId, themeMode, and categorySettingsJson. Also mention non-destructive migrations up to version 5.",
    qa: "Q: What does isSynced do? A: It marks whether a local row has been successfully written to Firestore.",
  },
  {
    title: "Firestore Structure & Queries",
    bullets: ["users/{uid}/profile/main", "users/{uid}/income/{incomeId}", "users/{uid}/expenses/{expenseId}", "users/{uid}/goals/{goalId}", "Remote sync compares updatedAtMillis"],
    visual: "tree",
    note: "Explain that Firestore collections are created automatically when documents are written. Also be honest: the repository uses these paths, while the current firestore.rules file appears older and should be updated to fully match the current schema.",
    qa: "Q: Do we manually create Firestore collections? A: No. Firestore creates collections when the app writes the first document.",
  },
  {
    title: "Sync Logic with WorkManager",
    bullets: ["SpendlySyncWorker + SyncManager", "Network constraint: connected", "Periodic sync: 15 minutes", "Immediate sync after sign-in and important writes", "Syncs profile, income, expenses, goals"],
    visual: "sync",
    note: "Explain that WorkManager is used because sync should be reliable and constraint-aware. The worker runs repository sync functions and retries failures up to configured attempts.",
    qa: "Q: Why not just sync from the screen? A: Screens can trigger immediate writes, but WorkManager handles delayed retry when network becomes available.",
  },
  {
    title: "Chamika Domain — Dashboard & Profile",
    bullets: ["Dashboard summary and recent transactions", "Profile view and settings", "Default currency and theme mode", "Password/profile actions", "User repository + profile DAO"],
    visual: "domain",
    domain: ["Dashboard", "Profile"],
    note: "Explain that Dashboard uses profile, transactions, and goals from FinanceUiState. Profile updates go through FinanceViewModel to UserRepositoryImpl, then Room and Firestore.",
    qa: "Q: Does Dashboard query Firestore directly? A: No. Dashboard reads ViewModel state derived from repository flows.",
  },
  {
    title: "Yesen Domain — Transactions & Create Account",
    bullets: ["Create account form and validation", "Add/edit income", "Add/edit expense", "History filters and grouped rows", "Income/expense repositories and DAOs", "Currency and category/source settings"],
    visual: "domain",
    domain: ["Create Account", "Transactions"],
    note: "Mention that income and expense are separate Room tables and Firestore subcollections, but they are combined into FinanceTransaction rows for display.",
    qa: "Q: Why separate income and expense tables? A: They have different fields and behavior, but are combined through TransactionRepository for UI lists and analytics.",
  },
  {
    title: "Nikini Domain — Login & Goals",
    bullets: ["Login and session handling", "Goal creation/edit/delete", "Icon suggestion and manual icon selection", "Progress and remaining amount calculation", "Add savings with target validation", "Goal saving creates linked Goal expense"],
    visual: "goal",
    note: "Highlight the important logic: adding money to a goal increases savedCents and creates an expense transaction with category Goal, so financial summaries remain consistent.",
    qa: "Q: Why create an expense when adding goal savings? A: It records money allocated away from available balance and connects goal progress with transaction history.",
  },
  {
    title: "Mahima Domain — Analytics, DB Setup & Sync",
    bullets: ["Analytics UI and ViewModel aggregation", "Room database foundation and migrations", "Entities and DAOs", "Firebase/Firestore setup notes", "WorkManager sync and caching logic"],
    visual: "analytics",
    note: "Explain that Analytics is calculated from real transaction data. AnalyticsViewModel filters by selected month and prepares totals, category percentages, committed/discretionary split, income sources, and 5-month overview.",
    qa: "Q: Are analytics hardcoded? A: No. They are derived from transactions observed through TransactionRepository.",
  },
  {
    title: "Main Feature Logic to Understand",
    bullets: ["Auth flow: Firebase session to app state", "Transaction flow: validate to Room to Firestore", "Dashboard sums by dateMillis", "Goals use savedCents / targetCents", "Analytics groups persisted transactions", "Profile settings sync through profile/main"],
    visual: "logic",
    labels: ["Auth", "Transactions", "Dashboard", "Goals", "Analytics", "Profile"],
    note: "This is a quick viva preparation slide. Use it to show the panel the logic you are ready to explain in code.",
    qa: "Q: Which date field is used for monthly reports? A: dateMillis, because it is the user-selected transaction event date. createdAtMillis is only audit metadata.",
  },
  {
    title: "UI/UX Design Decisions",
    bullets: ["Material 3 Compose UI", "Green/teal fintech theme", "Shared bottom navigation", "Shared floating plus action", "Shared month picker", "Dashboard-first information design", "Dark/light/system appearance support"],
    visual: "ui",
    note: "Explain that the UI is designed for repeated daily use. The app avoids long explanatory text in screens and uses cards, chips, forms, and quick actions to reduce friction.",
    qa: "Q: Why keep quick-add actions visible? A: Transaction entry must be low-friction, otherwise users abandon finance trackers.",
  },
  {
    title: "Live Demo Flow",
    bullets: ["0-1 intro", "1-3 architecture", "3-5 login/register", "5-7 dashboard", "7-9 transactions", "9-11 goals", "11-13 analytics/profile", "13-15 database/sync Q&A"],
    visual: "demo",
    labels: ["Intro", "Architecture", "Auth", "Dashboard", "Transactions", "Goals", "Analytics", "Q&A"],
    note: "Use this as the demo plan. The presentation should be paused here before the live app demo if time is short.",
    qa: "Q: What data should be prepared before demo? A: At least one income, several categorized expenses, one goal with savings, and transactions in the selected analytics month.",
  },
  {
    title: "Engineering Decisions & Justifications",
    bullets: ["MVVM: separation of UI and business logic", "Room + Firestore: offline cache plus cloud sync", "UID-based paths: user data isolation", "Money as cents: data accuracy", "WorkManager: reliable sync retry", "Hilt: dependency wiring"],
    visual: "decision",
    note: "This slide helps answer why questions. Emphasize tradeoffs: the team chose practical, maintainable patterns rather than putting everything in screens.",
    qa: "Q: Why use Hilt? A: It provides Firebase, Room, DAOs, repositories, ViewModels, and workers without manual factories.",
  },
  {
    title: "Limitations & Future Improvements",
    bullets: ["Current: Firestore rules need schema update", "Current: profile image is local URI only", "Current: offline delete conflict handling can improve", "Current: dedicated budgets/accounts not implemented", "Future: alerts, exports, notifications, advanced analytics, stronger sync"],
    visual: "roadmap",
    note: "Be honest and professional. Limitations do not weaken the project if they are clearly understood and realistic future work is proposed.",
    qa: "Q: What is the biggest technical improvement needed? A: Updating Firestore rules for the current schema and improving offline delete conflict handling.",
  },
  {
    title: "Conclusion / Q&A",
    bullets: ["Spendly connects daily tracking with financial awareness", "Local-first architecture improves reliability", "MVVM keeps the code explainable and maintainable", "Room + Firestore supports offline and cross-device data", "Ready for live demo and engineering questions"],
    visual: "closing",
    note: "Close by restating the value: the app turns fragmented income, expenses, and goals into one consistent system. Invite questions about architecture, database, sync, and feature logic.",
    qa: "Q: What is the strongest technical point of your project? A: The local-first MVVM architecture using Room, Firestore, repositories, StateFlow, Hilt, and WorkManager sync.",
  },
];

function add(slide, opts) {
  return slide.shapes.add({
    geometry: opts.geometry || "rect",
    position: { left: opts.x, top: opts.y, width: opts.w, height: opts.h },
    fill: opts.fill || { type: "solid", color: C.white },
    line: opts.line || { style: "solid", fill: "transparent", width: 0 },
  });
}

function text(slide, s, x, y, w, h, style = {}) {
  const box = add(slide, {
    x, y, w, h,
    fill: { type: "solid", color: style.fill || "transparent" },
    line: { style: "solid", fill: "transparent", width: 0 },
    geometry: style.geometry || "rect",
  });
  box.text.style = {
    typeface: style.font || "Aptos",
    fontSize: style.size || 22,
    color: style.color || C.ink,
    alignment: style.align || "left",
    verticalAlignment: style.valign || "top",
    bold: Boolean(style.bold),
    insets: style.insets || { top: 4, right: 4, bottom: 4, left: 4 },
    autoFit: style.autoFit || "shrinkText",
  };
  box.text = s;
  return box;
}

function card(slide, x, y, w, h, title, body, color = C.green, idx) {
  const c = add(slide, {
    geometry: "roundRect",
    x, y, w, h,
    fill: { type: "solid", color: C.white },
    line: { style: "solid", fill: C.line, width: 1 },
  });
  if (idx !== undefined) {
    const pill = add(slide, {
      geometry: "ellipse",
      x: x + 16, y: y + 18, w: 34, h: 34,
      fill: { type: "solid", color },
      line: { style: "solid", fill: "transparent", width: 0 },
    });
    pill.text.style = { typeface: "Aptos", fontSize: 14, color: C.white, bold: true, alignment: "center", verticalAlignment: "middle" };
    pill.text = String(idx);
  }
  text(slide, title, x + (idx !== undefined ? 62 : 18), y + 18, w - (idx !== undefined ? 80 : 36), 28, { size: 20, bold: true, color: C.ink });
  text(slide, body, x + 18, y + 54, w - 36, h - 66, { size: 15, color: C.muted });
  return c;
}

function titleBlock(slide, item, n) {
  text(slide, `0${n}`.slice(-2), 54, 34, 54, 28, { size: 14, bold: true, color: C.green, align: "center" });
  text(slide, item.title, 112, 28, 800, 50, { size: 31, bold: true, color: C.navy });
  add(slide, { x: 54, y: 88, w: 90, h: 4, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
}

function footer(slide, n) {
  text(slide, "Spendly technical demo", 54, 676, 240, 20, { size: 11, color: "#7B8A91" });
  text(slide, `${n}/24`, 1180, 676, 60, 20, { size: 11, color: "#7B8A91", align: "right" });
}

function bullets(slide, items, x = 72, y = 145, w = 500, gap = 44) {
  items.forEach((b, i) => {
    const yy = y + i * gap;
    add(slide, { geometry: "ellipse", x, y: yy + 7, w: 14, h: 14, fill: { type: "solid", color: i % 2 ? C.teal : C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
    text(slide, b, x + 28, yy, w - 28, 34, { size: 20, color: C.ink });
  });
}

function arrow(slide, from, to, opts = {}) {
  return slide.shapes.connect(from, to, {
    kind: opts.kind || "straight",
    fromSide: opts.fromSide || "right",
    toSide: opts.toSide || "left",
    line: { style: "solid", fill: opts.color || C.green, width: opts.width || 2 },
    head: { type: "arrow", width: "med", length: "med" },
  });
}

function phoneMock(slide, x, y, w, h) {
  add(slide, { geometry: "roundRect", x, y, w, h, fill: { type: "solid", color: "#0C1216" }, line: { style: "solid", fill: "#0C1216", width: 2 } });
  add(slide, { geometry: "roundRect", x: x + 12, y: y + 12, w: w - 24, h: h - 24, fill: { type: "solid", color: C.bg }, line: { style: "solid", fill: "transparent", width: 0 } });
  add(slide, { geometry: "roundRect", x: x + 70, y: y + 18, w: w - 140, h: 18, fill: { type: "solid", color: "#0C1216" }, line: { style: "solid", fill: "transparent", width: 0 } });
  add(slide, { geometry: "rect", x: x + 12, y: y + 42, w: w - 24, h: 112, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
  text(slide, "Good morning,", x + 34, y + 58, 120, 18, { size: 12, color: C.white });
  text(slide, "Kavindu", x + 34, y + 78, 150, 28, { size: 21, bold: true, color: C.white });
  add(slide, { geometry: "roundRect", x: x + 34, y: y + 120, w: w - 68, h: 58, fill: { type: "solid", color: C.mint }, line: { style: "solid", fill: "transparent", width: 0 } });
  text(slide, "Total Balance", x + 68, y + 128, w - 136, 20, { size: 11, bold: true, color: C.ink, align: "center" });
  text(slide, "LKR 127,213", x + 68, y + 148, w - 136, 26, { size: 20, bold: true, color: C.green, align: "center" });
  add(slide, { geometry: "roundRect", x: x + 30, y: y + 196, w: (w - 75) / 2, h: 54, fill: { type: "solid", color: C.white }, line: { style: "solid", fill: C.line, width: 1 } });
  add(slide, { geometry: "roundRect", x: x + 45 + (w - 75) / 2, y: y + 196, w: (w - 75) / 2, h: 54, fill: { type: "solid", color: C.white }, line: { style: "solid", fill: C.line, width: 1 } });
  text(slide, "Income\n215,413", x + 42, y + 205, 88, 40, { size: 12, color: C.green, bold: true });
  text(slide, "Expense\n88,200", x + 146, y + 205, 88, 40, { size: 12, color: C.red, bold: true });
  add(slide, { geometry: "roundRect", x: x + 30, y: y + 270, w: w - 60, h: 82, fill: { type: "solid", color: C.white }, line: { style: "solid", fill: C.line, width: 1 } });
  text(slide, "MacBook Pro M4", x + 52, y + 286, 160, 24, { size: 14, bold: true, color: C.ink });
  add(slide, { geometry: "roundRect", x: x + 52, y: y + 320, w: w - 104, h: 8, fill: { type: "solid", color: C.mint }, line: { style: "solid", fill: "transparent", width: 0 } });
  add(slide, { geometry: "roundRect", x: x + 52, y: y + 320, w: 94, h: 8, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
  add(slide, { geometry: "ellipse", x: x + w - 68, y: y + h - 90, w: 44, h: 44, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
  text(slide, "+", x + w - 67, y + h - 88, 42, 40, { size: 26, color: C.white, align: "center", valign: "middle" });
}

function visual(slide, item) {
  const rightX = 655;
  const top = 135;
  const w = 545;
  const h = 470;
  switch (item.visual) {
    case "title": {
      phoneMock(slide, 790, 88, 245, 445);
      card(slide, 80, 450, 220, 95, "Core stack", "Kotlin / Compose / MVVM", C.green, "A");
      card(slide, 320, 450, 220, 95, "Persistence", "Room local-first + Firestore", C.teal, "B");
      card(slide, 560, 450, 220, 95, "Sync", "Hilt + WorkManager", C.blue, "C");
      return;
    }
    case "timeline": {
      let prev = null;
      item.labels.forEach((label, i) => {
        const x = 640 + i * 112;
        const s = add(slide, { geometry: "ellipse", x, y: 268, w: 76, h: 76, fill: { type: "solid", color: i === 0 ? C.green : C.white }, line: { style: "solid", fill: C.green, width: 2 } });
        s.text.style = { typeface: "Aptos", fontSize: 18, bold: true, color: i === 0 ? C.white : C.green, alignment: "center", verticalAlignment: "middle" };
        s.text = `${i + 1}`;
        text(slide, label, x - 42, 360, 160, 42, { size: 15, bold: true, color: C.ink, align: "center" });
        if (prev) arrow(slide, prev, s, { color: C.teal, width: 2 });
        prev = s;
      });
      return;
    }
    case "persona": {
      card(slide, 675, 128, 230, 132, "Income streams", "Salary\nFreelance\nAdSense USD\nCrypto", C.green, "1");
      card(slide, 940, 128, 230, 132, "Expense channels", "Rent\nFood\nTransport\nGym\nSubscriptions", C.red, "2");
      const center = card(slide, 805, 330, 310, 126, "Problem", "Fragmented records make savings progress invisible.", C.teal, "!");
      text(slide, "Goal: MacBook Pro M4", 795, 506, 330, 40, { size: 24, bold: true, color: C.green, align: "center" });
      return center;
    }
    case "matrix": {
      item.matrix.forEach((m, i) => {
        const x = 660 + (i % 2) * 260;
        const y = 165 + Math.floor(i / 2) * 175;
        card(slide, x, y, 235, 132, m[0], m[1], [C.green, C.teal, C.purple, C.blue][i], i + 1);
      });
      return;
    }
    case "wheel": {
      add(slide, { geometry: "ellipse", x: 815, y: 250, w: 190, h: 190, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
      text(slide, "Spendly\nQuality", 850, 300, 120, 90, { size: 26, bold: true, color: C.white, align: "center", valign: "middle" });
      item.labels.forEach((l, i) => {
        const a = (Math.PI * 2 * i) / item.labels.length - Math.PI / 2;
        const x = 910 + Math.cos(a) * 225;
        const y = 345 + Math.sin(a) * 175;
        card(slide, x - 80, y - 34, 160, 68, l, "", [C.green, C.teal, C.blue, C.purple, C.amber, C.red][i], i + 1);
      });
      return;
    }
    case "flow":
    case "sequence":
    case "demo": {
      const labels = item.labels;
      let prev = null;
      labels.forEach((label, i) => {
        const x = 660 + (i % 4) * 135;
        const y = 170 + Math.floor(i / 4) * 170;
        const box = add(slide, { geometry: "roundRect", x, y, w: 112, h: 74, fill: { type: "solid", color: i === 0 ? C.green : C.white }, line: { style: "solid", fill: C.line, width: 1 } });
        box.text.style = { typeface: "Aptos", fontSize: 17, bold: true, color: i === 0 ? C.white : C.ink, alignment: "center", verticalAlignment: "middle" };
        box.text = label;
        if (prev && i % 4 !== 0) arrow(slide, prev, box, { color: C.green, width: 2 });
        prev = box;
      });
      return;
    }
    case "stack": {
      item.labels.forEach((label, i) => {
        const y = 160 + i * 70;
        const box = add(slide, { geometry: "roundRect", x: 745 - i * 20, y, w: 320 + i * 40, h: 52, fill: { type: "solid", color: [C.green, C.teal, C.blue, C.purple, C.amber, C.navy][i] }, line: { style: "solid", fill: "transparent", width: 0 } });
        box.text.style = { typeface: "Aptos", fontSize: 19, bold: true, color: C.white, alignment: "center", verticalAlignment: "middle" };
        box.text = label;
      });
      return;
    }
    case "architecture": {
      const ui = card(slide, 710, 135, 180, 80, "Compose UI", "Screens + components", C.green, "");
      const vm = card(slide, 710, 255, 180, 80, "ViewModel", "StateFlow + validation", C.teal, "");
      const repo = card(slide, 710, 375, 180, 80, "Repository", "Interfaces + impls", C.blue, "");
      const room = card(slide, 610, 505, 190, 78, "Room DB", "Entities + DAOs", C.purple, "");
      const fs = card(slide, 840, 505, 190, 78, "Firestore", "users/{uid}/...", C.green, "");
      arrow(slide, ui, vm, { fromSide: "bottom", toSide: "top" });
      arrow(slide, vm, repo, { fromSide: "bottom", toSide: "top" });
      arrow(slide, repo, room, { fromSide: "bottom", toSide: "top" });
      arrow(slide, repo, fs, { fromSide: "bottom", toSide: "top" });
      card(slide, 1010, 310, 170, 86, "WorkManager", "background retry", C.amber, "");
      return;
    }
    case "layers": {
      let prev = null;
      item.labels.forEach((label, i) => {
        const box = card(slide, 760, 135 + i * 88, 300, 62, label, ["render state", "validate + calculate", "data source decision", "local query", "remote document"][i], [C.green, C.teal, C.blue, C.purple, C.amber][i], i + 1);
        if (prev) arrow(slide, prev, box, { fromSide: "bottom", toSide: "top" });
        prev = box;
      });
      return;
    }
    case "cards": {
      item.labels.forEach((label, i) => {
        const x = 650 + (i % 2) * 260;
        const y = 130 + Math.floor(i / 2) * 105;
        card(slide, x, y, 235, 78, label, "screen state + logic", [C.green, C.teal, C.blue, C.purple, C.amber, C.red, C.navy][i], i + 1);
      });
      return;
    }
    case "er": {
      const u = card(slide, 815, 135, 230, 74, "UserProfile", "uid, currency, theme", C.green, "");
      const inc = card(slide, 620, 300, 210, 74, "IncomeEntry", "amountCents, source", C.teal, "");
      const exp = card(slide, 875, 300, 210, 74, "ExpenseEntry", "category, goalId", C.red, "");
      const goal = card(slide, 750, 470, 230, 74, "SavingsGoal", "targetCents, savedCents", C.blue, "");
      arrow(slide, u, inc, { fromSide: "bottom", toSide: "top" });
      arrow(slide, u, exp, { fromSide: "bottom", toSide: "top" });
      arrow(slide, u, goal, { fromSide: "bottom", toSide: "top" });
      arrow(slide, goal, exp, { fromSide: "right", toSide: "bottom", color: C.purple });
      return;
    }
    case "tree": {
      const root = card(slide, 805, 135, 230, 70, "users/{uid}", "Firebase owner key", C.green, "");
      ["profile/main", "income/{id}", "expenses/{id}", "goals/{id}"].forEach((label, i) => {
        const b = card(slide, 660 + (i % 2) * 275, 280 + Math.floor(i / 2) * 130, 235, 74, label, i === 0 ? "settings + profile" : "cloud collection", [C.teal, C.green, C.red, C.blue][i], i + 1);
        arrow(slide, root, b, { fromSide: "bottom", toSide: "top" });
      });
      return;
    }
    case "sync": {
      const a = card(slide, 675, 190, 180, 82, "Room", "isSynced=false", C.purple, "1");
      const b = card(slide, 910, 190, 180, 82, "Worker", "network connected", C.amber, "2");
      const c = card(slide, 910, 390, 180, 82, "Firestore", "upload docs", C.green, "3");
      const d = card(slide, 675, 390, 180, 82, "Room", "mark synced", C.teal, "4");
      arrow(slide, a, b);
      arrow(slide, b, c, { fromSide: "bottom", toSide: "top" });
      arrow(slide, c, d, { fromSide: "left", toSide: "right" });
      arrow(slide, d, a, { fromSide: "top", toSide: "bottom", color: C.blue });
      return;
    }
    case "domain": {
      card(slide, 680, 155, 235, 150, item.domain[0], item.domain[0] === "Dashboard" ? "summary cards\nrecent transactions\nlive state" : "forms\nvalidation\nrepositories", C.green, "A");
      card(slide, 945, 155, 235, 150, item.domain[1], item.domain[1] === "Profile" ? "settings\ncurrency\ntheme\npassword" : "history\nincome\nexpenses\ncreate account", C.teal, "B");
      card(slide, 760, 390, 360, 120, "Backend link", "ViewModel → Repository → DAO / Firestore", C.blue, "");
      return;
    }
    case "goal": {
      card(slide, 690, 155, 260, 120, "Goal", "MacBook Pro M4\nsavedCents / targetCents", C.green, "");
      card(slide, 930, 365, 245, 105, "Expense", "category = Goal\ngoalId linked", C.red, "");
      arrow(slide, slide.shapes.getItemAt?.(slide.shapes.items.length - 2) || slide.shapes.items[slide.shapes.items.length - 2], slide.shapes.items[slide.shapes.items.length - 1], { color: C.purple });
      add(slide, { geometry: "roundRect", x: 720, y: 295, w: 300, h: 12, fill: { type: "solid", color: C.mint }, line: { style: "solid", fill: "transparent", width: 0 } });
      add(slide, { geometry: "roundRect", x: 720, y: 295, w: 104, h: 12, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
      text(slide, "Progress drives UI + validation", 730, 322, 330, 30, { size: 20, bold: true, color: C.ink });
      return;
    }
    case "analytics": {
      add(slide, { geometry: "ellipse", x: 710, y: 210, w: 145, h: 145, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: C.white, width: 16 } });
      add(slide, { geometry: "ellipse", x: 746, y: 246, w: 73, h: 73, fill: { type: "solid", color: C.white }, line: { style: "solid", fill: C.white, width: 1 } });
      text(slide, "Category\nDonut", 728, 258, 110, 50, { size: 17, bold: true, color: C.ink, align: "center" });
      card(slide, 905, 145, 260, 85, "Monthly totals", "income vs expense", C.green, "1");
      card(slide, 905, 260, 260, 85, "Spending split", "committed / discretionary", C.purple, "2");
      card(slide, 905, 375, 260, 85, "5-month overview", "trend bars", C.blue, "3");
      return;
    }
    case "logic": {
      item.labels.forEach((label, i) => {
        const x = 650 + (i % 3) * 170;
        const y = 155 + Math.floor(i / 3) * 170;
        card(slide, x, y, 150, 112, label, ["session→state", "validate→save", "sum dateMillis", "saved/target", "group records", "profile/main"][i], [C.green, C.teal, C.blue, C.purple, C.amber, C.navy][i], i + 1);
      });
      return;
    }
    case "ui": {
      phoneMock(slide, 690, 125, 205, 382);
      card(slide, 940, 140, 220, 80, "Design system", "Material 3 + tokens", C.green, "");
      card(slide, 940, 250, 220, 80, "Navigation", "bottom tabs + shared FAB", C.teal, "");
      card(slide, 940, 360, 220, 80, "Theme", "System / Light / Dark", C.blue, "");
      return;
    }
    case "decision": {
      const rows = [["MVVM", "separate UI and logic"], ["Room + Firestore", "offline and cloud"], ["UID paths", "user isolation"], ["Cents", "money precision"], ["WorkManager", "reliable retry"], ["Hilt", "dependency wiring"]];
      rows.forEach((r, i) => {
        card(slide, 640 + (i % 2) * 290, 125 + Math.floor(i / 2) * 118, 265, 88, r[0], r[1], [C.green, C.teal, C.blue, C.purple, C.amber, C.navy][i], i + 1);
      });
      return;
    }
    case "roadmap": {
      card(slide, 650, 160, 255, 300, "Current limits", "Firestore rules need update\nLocal image URI only\nOffline delete conflict handling\nNo dedicated budgets/accounts", C.red, "Now");
      card(slide, 940, 160, 255, 300, "Future work", "Budget alerts\nPDF / CSV export\nNotifications\nAdvanced analytics\nStronger sync", C.green, "Next");
      return;
    }
    case "closing": {
      add(slide, { geometry: "ellipse", x: 820, y: 180, w: 210, h: 210, fill: { type: "solid", color: C.green }, line: { style: "solid", fill: "transparent", width: 0 } });
      text(slide, "Q&A", 850, 250, 150, 80, { size: 48, bold: true, color: C.white, align: "center", valign: "middle" });
      text(slide, "Architecture • Data • Sync • Demo", 700, 440, 520, 40, { size: 26, bold: true, color: C.ink, align: "center" });
      return;
    }
    default:
      return card(slide, rightX, top, w, h, "Visual", "Diagram placeholder", C.green, "");
  }
}

function buildSlide(presentation, item, idx) {
  const slide = presentation.slides.add();
  add(slide, { x: 0, y: 0, w: W, h: H, fill: { type: "solid", color: C.bg }, line: { style: "solid", fill: "transparent", width: 0 } });
  if (idx === 1) {
    add(slide, { x: 0, y: 0, w: W, h: H, fill: { type: "gradient", gradientKind: "linear", angleDeg: 0, stops: [{ offset: 0, color: C.greenDark }, { offset: 100000, color: C.navy }] }, line: { style: "solid", fill: "transparent", width: 0 } });
    text(slide, "Spendly", 80, 90, 360, 56, { size: 22, bold: true, color: C.mint });
    text(slide, item.title, 80, 150, 630, 155, { size: 48, bold: true, color: C.white });
    text(slide, item.kicker, 84, 315, 600, 38, { size: 24, color: C.mint });
    item.bullets.forEach((b, i) => {
      add(slide, { geometry: "ellipse", x: 88, y: 395 + i * 42, w: 12, h: 12, fill: { type: "solid", color: C.mint }, line: { style: "solid", fill: "transparent", width: 0 } });
      text(slide, b, 116, 385 + i * 42, 560, 34, { size: 21, color: C.white });
    });
    visual(slide, item);
    text(slide, "Live demo and engineering discussion", 80, 650, 480, 28, { size: 16, color: C.mint });
  } else {
    titleBlock(slide, item, idx);
    bullets(slide, item.bullets, 74, 145, 520, idx === 21 ? 38 : 42);
    visual(slide, item);
    footer(slide, idx);
  }
  slide.speakerNotes.text = `${item.note}\n\nPossible lecturer question and short answer:\n${item.qa}`;
}

async function main() {
  await fs.mkdir(OUT_DIR, { recursive: true });
  await fs.mkdir(PREVIEW_DIR, { recursive: true });
  await fs.mkdir(LAYOUT_DIR, { recursive: true });

  const presentation = Presentation.create({ slideSize: { width: W, height: H } });
  slides.forEach((s, i) => buildSlide(presentation, s, i + 1));

  for (let i = 0; i < presentation.slides.count; i += 1) {
    const slide = presentation.slides.getItem(i);
    const png = await presentation.export({ slide, format: "png", scale: 1 });
    await fs.writeFile(path.join(PREVIEW_DIR, `slide-${String(i + 1).padStart(2, "0")}.png`), Buffer.from(await png.arrayBuffer()));
    const layout = await presentation.export({ slide, format: "layout" });
    await fs.writeFile(path.join(LAYOUT_DIR, `slide-${String(i + 1).padStart(2, "0")}.layout.json`), await layout.text(), "utf8");
  }

  const pptx = await PresentationFile.exportPptx(presentation);
  await pptx.save(OUTPUT_PPTX);
  const stat = await fs.stat(OUTPUT_PPTX);
  await fs.writeFile(`${WORKSPACE}/output/build-manifest.json`, JSON.stringify({
    output: OUTPUT_PPTX,
    bytes: stat.size,
    slideCount: presentation.slides.count,
    previewDir: PREVIEW_DIR,
  }, null, 2));
  console.log(JSON.stringify({ output: OUTPUT_PPTX, bytes: stat.size, slideCount: presentation.slides.count }, null, 2));
}

main().catch((err) => {
  console.error(err.stack || err.message || String(err));
  process.exit(1);
});
