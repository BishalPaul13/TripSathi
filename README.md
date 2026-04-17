# 🌍 TripSathi - Smart Tourist Safety Monitoring System

**TripSathi** is a premium, AI-powered travel safety companion designed to protect tourists through real-time monitoring, proactive incident response, and verifiable digital identification. Built with modern Android practices, it integrates Geo-fencing, Firebase, and a community-driven safety network to ensure peace of mind for every traveler.

---

## ✨ Key Features

### 🛡️ Proactive Safety & SOS
*   **One-Tap Emergency SOS**: Immediately triggers an alarm, vibrates, and sends a **Multipart SMS** with the user's real-time Google Maps location to emergency contacts.
*   **Geo-Fence Protection**: Automatically detects when a user enters "Danger Zones" and sends instant haptic and visual warnings.
*   **Safe Path Assistant**: A multi-step interactive chat assistant that guides users toward well-lit, safe routes when they feel vulnerable.

### 🆔 Verified Identity & Documents
*   **Digital ID (QR-based)**: Encodes vital user info (Name, Blood Group, Emergency Contact) into a secure QR code for first responders.
*   **Verify Guide/Driver**: Instantly scan QR codes of tourist guides or drivers to verify their credentials against our secure registry.
*   **Secure Document Vault**: Store Aadhaar and PAN numbers in the cloud while keeping sensitive document photos saved **strictly locally** for maximum privacy.

### 🏥 Health & Assistance
*   **Emergency Medical Card**: Quick access to blood group, allergies, chronic conditions, and medications for medical teams.
*   **Nearby Hospital Locator**: Dynamic map integration to find the nearest critical care centers in real-time.

### 🗺️ Smart Tourism
*   **Nearby Famous Places**: Automatically identifies and sorts famous landmarks based on your current GPS location.
*   **Safe-Stay Ratings**: A crowd-sourced map where travelers rate hotels and areas based on "Night-time safety" and "Solo-traveler friendliness."

### 🌐 Global Accessibility
*   **Multi-Language Support**: Fully functional in **English**, **Hindi (हिंदी)**, and **Telugu (తెలుగు)** with seamless on-the-fly switching.

---

## 🛠️ Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Modern Declarative UI)
*   **Backend**: Firebase (Authentication, Realtime Database)
*   **Maps**: Google Maps SDK for Android
*   **Location**: Google Play Services (Fused Location Provider)
*   **Architecture**: Activity-based with a centralized `BaseActivity` for global locale management.

---

## 📸 Screenshots
*(Coming Soon - Check the `screenshots` folder)*

---

## 🚀 Getting Started
1.  **Clone the Repo**: `git clone https://github.com/LakshitRaina/TripSathi.git`
2.  **Add Firebase**: Place your `google-services.json` in the `app/` directory.
3.  **Build**: Open in Android Studio and run on an emulator or physical device.

---

## 👨‍💻 Developer
**Lakshit Raina**  
*Building technology that makes travel safer for everyone.*

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
