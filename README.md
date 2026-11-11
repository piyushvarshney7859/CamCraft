# CamCraft 📸

CamCraft is an advanced, fully-functional camera and gallery application. It leverages the CameraX library for capturing images and adheres strictly to modern Android scoped storage guidelines (MediaStore) to securely save photos without requesting broad storage permissions.

## Features
* **Live CameraX Preview:** Full-screen camera feed embedded natively within a Jetpack Compose UI.
* **Camera Controls:** Seamlessly toggle between front and back lenses and cycle through flash modes (Auto, On, Off).
* **Secure MediaStore Saving:** Saves captured images directly to the public `Pictures/CamCraft` directory.
* **Integrated Gallery:** A fast, staggered photo grid built with `LazyVerticalGrid` and Coil for smooth background image loading.
* **EXIF Metadata Extraction:** Reads raw image bytes to display hardware timestamps and resolution details.
* **Image Editing:** Integrates the uCrop library for high-performance cropping and rotation, safely saving edits back to the gallery.
* **Native Sharing:** Uses implicit intents to share photos directly to social platforms like WhatsApp or Instagram.

## Tech Stack
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose & Material 3
* **Camera API:** AndroidX CameraX 
* **Storage:** MediaStore API (Scoped Storage)
* **Image Loading:** Coil
* **Image Editing:** Yalantis uCrop
* **Permissions:** Google Accompanist

## Getting Started
1. Clone this repository: `git clone https://github.com/piyushvarshney7859/CamCraft.git`
2. Open the project in Android Studio.
3. Run on a **physical Android device** (CameraX features may behave unreliably on virtual emulators).
