# MISSION: Create Native Android ISP Customer Application
# ROLE: Senior Android Engineer & System Architect

## 1. OBJECTIVE
Build a high-performance, modern, and user-friendly Android application for Internet Service Provider (ISP) customers. The app will enable customers to manage their internet subscription, view and pay bills, report connection issues (ticketing), monitor their internet status, and receive real-time updates.

The application must be clean, secure, and production-ready, utilizing a robust MVVM architecture in Java with modern design aesthetics.

## 2. TECH STACK & ARCHITECTURE
- **Language:** Java 17
- **Min SDK:** 24 (Android 7.0)
- **UI Toolkit:** XML Layouts (View System) with Material Components 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel) with Repository Pattern
- **Local Database:** Room Database for caching user profile, bills, and tickets
- **Networking:** Retrofit 2 with OkHttp for REST API communication
- **Authentication:** Firebase Auth / Custom API token-based auth
- **Notifications:** Firebase Cloud Messaging (FCM) for push notifications
- **Dependency/Utilities:**
  - ViewBinding (mandatory)
  - ExecutorService / WorkManager for background processing
  - EncryptedSharedPreferences for securing sensitive tokens and user info

## 3. CORE FEATURES FOR ISP CUSTOMER APP
1. **User Authentication & Onboarding**:
   - Secure login (using Email/Password or Customer ID & Password).
   - Remember Me / Auto-login.
   - User Profile management (view subscription details, NIK, address, contact info).
2. **Dashboard & Subscription Overview**:
   - Connection status (Active, Suspended, Maintenance) with clear visual indicators.
   - Active package details (bandwidth speed, monthly price, active period).
   - Quick diagnostics / Connection speed test entry point.
3. **Billing & Invoices**:
   - Active invoice summary (Amount due, due date, payment status).
   - Invoice list & Payment history (downloadable PDF invoices if supported).
   - Payment integration gateway placeholders (e.g., Midtrans, virtual accounts, bank transfers).
4. **Complaint & Ticketing System**:
   - Create new support tickets with categories (e.g., No Connection, Slow Connection, Billing Issue).
   - Real-time ticket status tracking (Open, In Progress, Scheduled, Resolved).
   - Service history & technician visit schedules.
5. **Broadcasts & Notifications**:
   - Push notifications for billing reminders, network maintenance schedules, and ticket updates.
   - In-app Notification Center/Inbox.

## 4. ARCHITECTURAL & SECURITY CONSTRAINTS
- **View Binding:** ViewBinding must be enabled. Do NOT use `findViewById` or raw ButterKnife.
- **Secure Storage:** All authentication tokens, customer IDs, and sensitive preferences must be stored in `EncryptedSharedPreferences`.
- **Background Tasks:** Use `WorkManager` for persistent tasks (e.g. syncing data in background) and `ExecutorService` for transient background threads. Avoid blocking the Main UI Thread.
- **UI & UX:** Apply a premium dark-themed or modern polished HSL color scheme with Material 3. Use micro-animations, clean card components, and shimmers/loading indicators for empty states.
- **Error Handling:** Gracefully handle network failures, showing offline states, and structured error messages to the user.

## 5. PROJECT DIRECTORY STRUCTURE
- `com.androidstarter.app.data`: Local database, models, preferences, API clients, and repositories.
- `com.androidstarter.app.ui`: Activities, Fragments, ViewModels, and Adapters grouped by features (auth, dashboard, billing, tickets, notifications, settings).
- `com.androidstarter.app.utils`: Helper utilities (connection checkers, date formatters, dialog builders, secure storage helpers).
- `com.androidstarter.app.services`: Firebase Messaging Service, background sync services.