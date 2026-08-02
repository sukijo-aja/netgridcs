# MISSION: Create Native Android App Boilerplate (Legacy Stack)
# ROLE: Senior Android Engineer

## 1. OBJECTIVE
Create a clean, reusable Android starter boilerplate application using Java. The app is a foundational generic template with package name com.starterandroid.app

## 2. TECH STACK
- **Language:** Java 17
- **Min SDK:** 24 (Android 7.0)
- **UI Toolkit:** XML Layouts (View System)
- **Architecture:** MVVM (Model-View-ViewModel) - *Yes, MVVM is possible in Java and better than MVC.*
- **Database:** Room Database (Java implementation)
- **Networking:** Retrofit 2 (if needed)

## 3. KEY CONSTRAINTS (Crucial for Java)
- **View Binding:** MUST use ViewBinding enabled in `build.gradle`. Do NOT use `findViewById` as it is error-prone.
- **Concurrency:** Use `ExecutorService` or `RxJava2` for background tasks (since we cannot use Kotlin Coroutines). Do not run heavy tasks on the UI thread.
- **Material Design:** Use Material Components 3 (`com.google.android.material`) for UI elements.

## 4. EXECUTION PLAN
1. Setup `build.gradle` (Module level) to enable ViewBinding and add Room/Retrofit dependencies.
2. Ensure basic UI layouts (e.g. MainActivity) are clean using ConstraintLayout.
3. Maintain generic Base Activity and Fragment for reusability.
4. Implement basic examples of ViewModel and Repository pattern in Java.
5. Connect everything seamlessly with ViewBinding.
6. Ensure standard Android permissions (Internet, etc.) in AndroidManifest.xml.
7. Include placeholder implementation for common utilities like Retrofit API Service, Room DB.
8. Maintain a cleanly layered architecture (UI, Data, Domain).
9. Setup Bottom Navigation with basic generic fragments (Home, Profile, Settings).
10. Provide base styles and symbols (launcher icons, colors, themes).
11. Ensure `android.useAndroidX=true` in `gradle.properties`.
12. Include a basic Splash Screen implementation.
13. Clean up any domain-specific features from previous projects.

## 5. OUTPUT
Produce a clean, compilable boilerplate project structure that I can open directly in Android Studio and use as a starting point for any new Android applications.