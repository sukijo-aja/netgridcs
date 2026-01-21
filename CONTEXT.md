# MISSION: Create Native Android App (Legacy Stack)
# ROLE: Senior Android Engineer

## 1. OBJECTIVE
Create a functional Android application using Java. The app is a Prayer Time Reminder App. with package name com.mosleemreminder.app

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
2. Create the Layout XML files first using ConstraintLayout.
3. Create the Entity and DAO for Room Database.
4. Implement the ViewModel and Repository pattern in Java.
5. Connect everything in the MainActivity using ViewBinding.
6. Add the necessary permissions in the AndroidManifest.xml file.
7. Implement dynamic location (GPS)
8. Implement the PrayerTimeAdapter to display the prayer times in a RecyclerView.
9. Add "Next Prayer" countdown.
10. Improve UI styling.
11. create Symbol @drawable/ic_launcher, @drawable/ic_launcher_foreground, resource color/purple_700,  resource drawable/ic_launcher_background
12. set android.useAndroidX=true in gradle.properties
13. add splash screen
14. add notification
15. add Al Quran and Hadist with detail read text arabian justify right, also last read
16. Get data Al Quran and Hadist from Internet and save at Local Storage SQLite, With Consep Local First
17. Add searching Al Quran and Hadist, with read detail Al Quran and Hadist with indonesia translate
18. add bottom navigation fragment setting
19. dont include Bismilah at detail ayah




## 5. OUTPUT
Produce a clean, compilable project structure that I can open directly in Android Studio.