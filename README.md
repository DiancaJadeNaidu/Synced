# Synced

**Synced** is a Kotlin-based mobile application designed to help people build meaningful friendships and relationships through compatibility-based matching, interactive features, and real-time engagement.

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-green)

**Status:** The app is currently in **Closed Testing**.

---

## Group Members

- Dianca Jade Naidu  
- Riva Jangda  
- Keira Meth  
- Azande Mnguni  
- Asanda Dimba  

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Additional POE Features](#additional-poe-features)
- [Architecture / Components](#architecture--components)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [Contact](#contact)
- [References](#references)

---

## Overview

**Synced** is a Kotlin Android application that connects users based on compatibility across personality, interests, and lifestyle traits.  
Users can choose their intent (friendship or romantic), play games together, and interact using real-time features.

The app integrates with a **.NET REST API** for profiles, scoring, safety features, notifications, and multiplayer interactions.

The project is fully prepared for **Google Play Store deployment** and is currently running in **Closed Testing**.

---

## Features

### **USER FEATURE 1 — Questionnaire & Matching**
- Users complete a personality and lifestyle questionnaire.
- Compatibility scores are calculated automatically.
- Matches update dynamically as preferences change.

### **USER FEATURE 2 — Flexible Intent**
Users choose:
- **Friendship**, or  
- **Romantic relationships**

Intent can be switched anytime, and matching adjusts accordingly.

### **USER FEATURE 3 — Multiplayer Games**
- Fun in-app multiplayer games (trivia, memory match, etc.)
- Leaderboards and scoring fully synced with the backend.

---

## Additional POE Features

### **1. Geolocation Matching + Ghost Mode**
Using Android’s fused location services, Synced introduces:

**Location-Based Matching**
- Find users nearby  
- Distance-based match sorting  

**Ghost Mode**
- Hides real-time location  
- Masks distance metadata  
- Shows only compatibility-based matches  

This gives users full control over privacy.

---

### **2. Blocking & Reporting (Safety & Moderation)**
Synced includes a complete safety and moderation system:

**Blocking**
- Prevents interaction  
- Removes users from visibility lists  
- Disables messaging, requests, and game invites  

**Reporting**
- Allows users to report inappropriate behaviour  
- Reports are forwarded to the API’s moderation endpoint  

This ensures a safe and secure user environment.

---

### **3. Multi-Language Support (EN / Afrikaans / isiZulu)**
All screens and UI components support:

- 🇬🇧 English  
- 🇿🇦 Afrikaans  
- 🇿🇦 isiZulu  

Using Android resource localization for all layouts, dialogs, and strings.

---

### **4. Offline Sync Mode (RoomDB)**
Synced works seamlessly offline using **RoomDB**.

Offline-supported features include:
- User profile  
- User settings  
- Sending connection requests  
- Accepting/declining requests  

Data automatically syncs when internet returns.

---

### **5. Real-Time Notifications**
Using Firebase Cloud Messaging (FCM), users receive live notifications:

- When receiving a new request  
- When a request is accepted or declined  
- When receiving game invites  
- Even when the app is closed  

Notifications work both in-app and background.

---

### **6. Automated Testing (GitHub Actions CI/CD)**
Synced includes automated test pipelines:

- Unit Testing  
- UI Testing  
- Lint Checks  
- Static Code Analysis  
- Build & APK Validation  
- API Endpoint Testing  


---

### **7. Biometric Login (Fingerprint & Face Unlock)**
Synced supports secure login using:

- Fingerprint authentication  
- Face unlock (device-supported)  
- Secure fallback authentication  

Credentials use encrypted storage with Android Biometrics.

---

### **8. Google Play Store Preparation**
Prepared for Play Store with:

- Signed release build  
- Privacy policy  
- Optimized icons & adaptive icons  
- Content rating and metadata  
- Play Store screenshots  
- Closed Testing rollout configuration  

---

### **9. API Improvements (Latest Version)**
As part of the final update:

- Fixed core authentication endpoints  
- Added new endpoints for blocking, reporting, games, and geolocation  
- Improved pagination & filtering  
- Optimized response models  
- Fixed intermittent server errors  
- Enhanced error handling and validations  

---

### **10. UI Component Enhancements**
Final version includes:

- Updated layout spacing & alignment  
- Improved accessibility (font scaling, contrast)  
- Cleaner animations & transitions  
- Better error states & loading indicators  
- Standardized color palette and typography  

---

## Architecture / Components

- **Kotlin** — Primary language  
- **RoomDB** — Offline storage & sync  
- **Firebase Cloud Messaging** — Push notifications  
- **Android BiometricPrompt** — Secure login  
- **.NET REST API** — Backend service  
- **Gradle** — Build system  
- **GitHub Actions** — CI/CD automation  

---

## Getting Started

### Requirements

- Android Studio  
- Kotlin 1.9+  
- Gradle 8+  
- Android SDK 24+  

---

### Installation  

1. Clone the repository:  
   ```bash
   git clone https://github.com/DiancaJadeNaidu/Synced.git
   cd Synced
2. Open the project in Android Studio.
3. Let Gradle sync dependencies.

### Running the App

-Connect an Android device or start an emulator.

-Run the project via Android Studio using:

./gradlew installDebug

---

## Usage

1. Sign up and complete your questionnaire.

2. View your compatibility matches for friendship or relationships.

3. Start chatting or playing games with new connections.

4. Continue exploring and syncing with others.

---

## Configuration

Configuration values can be set in the project’s gradle.properties or local.properties. Examples include:

Name	Description	Default Value
SYNC_SERVER_URL	URL for syncing compatibility data	Localhost
DB_PATH	Local storage path for app data	synced.db

---

## Contributing

We welcome contributions!

- Fork the repo

- Create a feature branch (git checkout -b feature/YourFeature)

- Commit changes (git commit -m "Add feature")

- Push to branch (git push origin feature/YourFeature)

- Open a Pull Request

---

## License

This project is licensed under the MIT License. See the LICENSE
 file for details.

---
## Contact

Maintainer: Dianca Jade Naidu
GitHub: https://github.com/DiancaJadeNaidu

Repo: https://github.com/DiancaJadeNaidu/Synced

---

## References

Google (2025) Android developers guide. Available at: https://developer.android.com/guide
 (Accessed: 6 October 2025).

Gradle (2025) Gradle user guide. Available at: https://docs.gradle.org/current/userguide/userguide.html
 (Accessed: 6 October 2025).

JetBrains (2025) Kotlin documentation. Available at: https://kotlinlang.org/docs/home.html
 (Accessed: 6 October 2025).

Firebase (2025) Firebase documentation. Available at: https://firebase.google.com/docs
 (Accessed: 6 October 2025).

WhatsApp (2025) WhatsApp business platform: Cloud API documentation. Available at: https://developers.facebook.com/docs/whatsapp
 (Accessed: 6 October 2025).

Instagram (2025) Instagram graph API documentation. Available at: https://developers.facebook.com/docs/instagram-api
 (Accessed: 6 October 2025).





