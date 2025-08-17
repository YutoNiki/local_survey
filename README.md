# Local Survey App

![Local Survey Banner](opma_banner.png)

This is a simple survey application for Android tablets, designed to collect user feedback offline. It's ideal for gathering satisfaction ratings at events, exhibitions, or any location without internet access.

## Language Support

This application supports both Japanese and English languages, adapting to the device's language settings.

![Local Survey Banner](opma_banner.png)

This is a simple survey application for Android tablets, designed to collect user feedback offline. It's ideal for gathering satisfaction ratings at events, exhibitions, or any location without internet access.

## Language Support

This application supports both Japanese and English languages, adapting to the device's language settings.

## Features

- **Four-Level Satisfaction Rating:** Users can record their satisfaction level by tapping one of four emoji buttons:
    - 😊 Very Satisfied
    - 🙂 Satisfied
    - 😐 Unsatisfied
    - 😠 Very Unsatisfied
- **Cooldown Mechanism:** To prevent accidental or fraudulent multiple clicks, there is a 5-second cooldown period after a button is pressed.
- **Timestamped Data Logging:** Each rating is saved with a precise timestamp.
- **Offline Operation:** The app works completely offline. No internet connection is required.
- **In-App Log Viewer:** View all recorded survey logs directly within the app.
- **Data Sharing/Export:** Easily share the `survey_log.csv` file with other applications (e.g., email, cloud storage) for further analysis.

## How to Use

1.  Launch the app on your Android tablet.
2.  Tap one of the four emoji buttons to record your feedback.
3.  A "Thank you" message will appear, and the buttons will be disabled for 5 seconds.
4.  After 5 seconds, the app is ready to receive the next feedback.
5.  **To view logs:** Tap the menu icon (three lines) in the top right corner of the survey screen.
6.  **To share logs:** On the log viewer screen, tap the share icon (arrow) in the top right corner and choose your preferred sharing method.

## Data Storage

- The survey data is stored in a CSV file named `survey_log.csv`.
- **File Location:** The file is located in the app's internal storage directory. You can typically access it using a file manager app (requires Android Studio's Device File Explorer for direct access on Android 11+):
  `/Android/data/com.example.local_survey/files/survey_log.csv`
- **CSV Format:** The data is stored in the following format:
  `timestamp,rating`
  (e.g., `2025-07-25 10:30:00,Very Satisfied`)

## Building from Source

### Prerequisites

- Android Studio
- JDK 17

### Build Instructions

1.  Clone the repository:
    ```bash
    git clone <repository-url>
    ```
2.  Open the project in Android Studio.
3.  Connect an Android device or start an emulator.
4.  Build and run the app.

Alternatively, you can build from the command line using Gradle:

```bash
./gradlew installDebug
```