# Synced

**Synced** is a Kotlin-based mobile application that helps people connect and build meaningful relationships.  

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)](https://kotlinlang.org/)  
[![License: MIT](https://img.shields.io/badge/License-MIT-green)](LICENSE)  

---

## Group Members  

- Member 1: Dianca Jade Naidu  
- Member 2: Riva Jangda  
- Member 3: Keira Meth  
- Member 4: Azande Mnguni  
- Member 5: Asanda Dimba  

---

## Table of Contents

- [Overview](#overview)  
- [Features](#features)  
- [Architecture / Components](#architecture--components)  
- [Getting Started](#getting-started)  
  - [Requirements](#requirements)  
  - [Installation](#installation)  
  - [Running the App](#running-the-app)  
- [Usage](#usage)  
- [Configuration](#configuration)  
- [Contributing](#contributing)
- [Contact](#contact) 
- [License](#license)  
- [References](#references)  

---

## Overview  

**Synced** is a Kotlin-based mobile application that helps people connect and build meaningful relationships.  
The app matches users based on a questionnaire that evaluates compatibility, allowing them to decide whether they are looking for a friendship or a romantic relationship.  

Beyond just matching, Synced makes the experience engaging by including interactive in-app games that users can play together, helping break the ice and strengthen connections.

The goal is to create a safe, fun, and personalized environment for meeting new people, making friends, or exploring relationships.

The app communicates with the Synced API (a RESTful API built in .NET) for managing user profiles, compatibility scores, leaderboards for games, and multiplayer competition tracking.

---

## Features  

**USER FEATURE 1**
*Questionnaire & Matching* -

On sign-up, users complete a compatibility questionnaire covering personality traits, interests, and lifestyle choices.

This questionnaire data is securely stored in Firebase, which powers the matchmaking engine.

The app uses this data to suggest potential matches with high compatibility scores, improving the chance of meaningful connections.

Matching is dynamic and updated as users change their preferences, ensuring results always stay relevant.

**USER FEATURE 2**
*Flexible Intent* - 

Users can choose whether they are seeking friendship or a romantic connection.

Since people often change their minds over time, Synced allows them to switch intent seamlessly.

If a user selects friendship, they will only be shown others who also chose friendship; the same applies to romance.

This keeps matches aligned with intent, helping avoid mismatched expectations.

**USER FEATURE 3**
*Interactive In-App Games* - 

To help break the ice, Synced includes multiplayer mini-games (such as trivia, memory match, or simple challenges).

These games are designed for quick, fun engagement, making conversations feel natural instead of forced.

Game scores, player stats, and leaderboards are managed through the Synced API, creating a competitive but social layer to user interactions.

Users can see how they rank against their matches, sparking fun conversations and giving them more reasons to stay connected.

---

## Architecture / Components  

- **Kotlin (100%)** — Core language for app development  
- **Gradle** — Build automation and dependency management  
- **App Module** — Android UI, questionnaire, and games  
- **Sync Module** — Compatibility engine and matching logic  
- **CI/CD** — GitHub Actions workflows for automated builds and checks  

---

## Getting Started  

### Requirements  

- [Android Studio](https://developer.android.com/studio)  
- Kotlin 1.9+  
- Gradle 8+  
- Android SDK 24+  

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





