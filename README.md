# SAM AI — Smart Autonomous Machine

> Premium Android AI Assistant • Kotlin • Jetpack Compose • Material 3

---

## Project Structure

```
SAM AI/
├── app/src/main/java/dev/shlok/samai/
│   ├── ai/               # AI providers (OpenAI, Gemini, Custom), Orchestrator, Intent detection
│   ├── androidcontrol/   # AccessibilityService, AppLauncher, AndroidController
│   ├── di/               # Hilt dependency injection modules
│   ├── knowledge/        # Wikipedia API provider
│   ├── memory/           # Room database, MemoryRepository
│   ├── profile/          # UserPreferences (DataStore), DeveloperProfile
│   ├── tasks/            # Task, TaskPlanner, TaskExecutor
│   ├── ui/               # All Compose screens + components + theme
│   └── voice/            # SpeechRecognizer, TextToSpeech
└── app/src/main/res/     # Resources, icons, themes, XML configs
```

---

## How to Build in Android Studio

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 (bundled with Android Studio)
- Android SDK 35 installed
- Gradle 8.7 (downloaded automatically)

### Steps
1. Open Android Studio → **File → Open** → select this folder
2. Wait for Gradle sync to complete
3. Update `local.properties` with your SDK path:
   ```
   sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   ```
4. Click **Run ▶** or press `Shift+F10`

---

## First Launch Setup

1. **Splash screen** appears immediately — no white flash
2. On **Home screen**, tap the **Mic button** → grant microphone permission
3. Go to **Settings → Accessibility Service** → tap **Open Accessibility Settings** → enable **SAM AI Assistant**
4. Go to **Settings → AI Models** → add your API key:
   - **OpenAI**: get from [platform.openai.com](https://platform.openai.com)
   - **Gemini**: get from [aistudio.google.com](https://aistudio.google.com)

---

## Build Debug APK

```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

---

## Build Release APK

### 1. Generate a keystore (one-time)
```bash
keytool -genkeypair -v -keystore sam-ai-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias sam-ai -storepass YOUR_STORE_PASS -keypass YOUR_KEY_PASS
```

### 2. Add signing config to `app/build.gradle.kts`
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("sam-ai-release.jks")
            storePassword = "YOUR_STORE_PASS"
            keyAlias = "sam-ai"
            keyPassword = "YOUR_KEY_PASS"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

### 3. Build
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

---

## Features

| Feature | Status |
|---|---|
| Animated 3D SAM Core | ✅ |
| Splash screen (no blank flash) | ✅ |
| Voice input / Speech-to-Text | ✅ |
| Text-to-Speech responses | ✅ |
| AI Chat (OpenAI / Gemini / Custom) | ✅ |
| Android app launcher | ✅ |
| Accessibility Service control | ✅ |
| Multi-step Task Planner | ✅ |
| Task Center with live progress | ✅ |
| Persistent Memory (Room) | ✅ |
| Wikipedia Knowledge | ✅ |
| Coding Studio + HTML Preview | ✅ |
| Developer Profile (Shlok) | ✅ |
| Settings + Personalization | ✅ |
| Offline graceful handling | ✅ |
| Dark futuristic UI | ✅ |
| Custom adaptive app icon | ✅ |

---

## Developer

**Shlok** — Data Science Student & AI Enthusiast
- GitHub: [theshlok18](https://github.com/theshlok18)
- Instagram: [@iishlok23](https://instagram.com/iishlok23)

*SAM AI was inspired by the concept of Tony Stark's JARVIS.*
