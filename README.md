# AI Document Scanner (Android)

Scan a document with the camera, read the text on-device, store it offline, and get an AI summary on demand.

## Features
- **CameraX** capture inside a Jetpack Compose screen
- **ML Kit Text Recognition** runs on-device (works offline, image never leaves the phone)
- **Room** as the single source of truth, exposed as `Flow`
- **AI summary** through a small backend proxy (Gemini API), so **no API key is shipped in the APK**
- **Hilt**, **Navigation Compose**, MVVM + Clean Architecture layers
- **Unit tests** with JUnit, MockK and Turbine

## Architecture

```
presentation (Compose + ViewModel, StateFlow)
      |  use cases
domain   (Scan model, ScanRepository interface, use cases)
      |
data     (Room ScanDao, Retrofit SummaryApi, ScanRepositoryImpl)
ml       (TextRecognizerService - ML Kit)
```

Layers are separated by package. To go fully modular, split them into `:core:domain`, `:core:data` and `:feature:scans` Gradle modules.

## Run

1. Start the proxy (Node 18+):
   ```
   cd server
   GEMINI_API_KEY=your_key node server.js
   ```
   Get a key from https://aistudio.google.com/. Never commit it.
2. Open the project in Android Studio, run on an emulator (it reaches your machine at `10.0.2.2:8080`) or a device.
   For a real device, set `SUMMARY_API_BASE_URL` in `gradle.properties` to your server's HTTPS URL.
3. Tap **+**, capture a page, then tap **Summarize with AI**.

## Tests
```
gradle testDebugUnitTest
```

## Security notes
- API key lives only on the server (`GEMINI_API_KEY` env var).
- Cleartext HTTP is enabled for **debug builds only**; release builds require HTTPS.
- The proxy limits request size and rate per IP. Add authentication (for example Firebase App Check or a signed token) before using it publicly.
- `android:allowBackup="false"` so scanned text is not copied into device backups.

## Ideas to extend
PDF export, image-to-PDF with ML Kit Document Scanner, encrypted Room (SQLCipher), on-device summaries with Gemini Nano, Paging 3 for large histories.
