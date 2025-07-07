
# 🚲 Eco Ride

**Eco Ride** is a sustainable and intelligent ride-hailing app designed with modern Android technologies. It enables users to securely log in, locate rides via Google Maps, make payments through Razorpay, and ensures driver authenticity through a document verification process.

Built using **Jetpack Compose** for a smooth and dynamic UI experience and powered by **Firebase** backend services.

---

## 🌟 Key Features

- 🔐 **Multi-Authentication System**
  - Login using:
    - Username & Password
    - Google Account
    - Facebook
    - Mobile Number (OTP-based)

- 📍 **Google Maps Ride Discovery**
  - Find nearby rides
  - Real-time map updates
  - Location detection and route suggestions

- 💳 **Payment Integration with Razorpay**
  - Seamless payment gateway
  - Transaction history and confirmation

- ✅ **Driver Verification Module**
  - Upload required documents:
    - Address Proof
    - Driving License
    - Vehicle Registration Certificate (RC)
  - Secure cloud storage via Firebase

---

## 🖼️ UI Screenshots

_Add your app screenshots in the `/screenshots` folder and reference them here:_

> Example:  
> ![Login Screen](https://github.com/sainikhil131/Eco-Ride/blob/5139abe9f494ea9ea59418f9482957fce58dac47/01.jpg)  
> ![Ride Finder](https://github.com/sainikhil131/Eco-Ride/blob/58a5f3a345fdc0668d3996ffd3a4ae64db376bb5/03.jpg)  
> ![Publish Ride](https://github.com/sainikhil131/Eco-Ride/blob/383efaff5dc64cfc5e045415fb209e33d4994e68/04.jpg)

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **IDE:** Android Studio
- **Backend:** Firebase (Authentication, Firestore, Storage)
- **APIs & SDKs:**
  - Google Maps SDK
  - Razorpay Payment Gateway
  - Firebase Authentication & Realtime Database

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/eco-ride.git
````

### 2. Open the Project

* Launch **Android Studio**
* Open the cloned project folder

### 3. Setup API Keys

* Replace the placeholder files with your own:

  * `google-services.json` (for Firebase)
  * Google Maps API Key (`local.properties` or manifest)
  * Razorpay API Key (in secure file or build config)

> ⚠️ **Do not commit your API keys to GitHub. Use `.gitignore` to exclude them.**

---

## 📦 Project Structure

```
EcoRide/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ecoride/
│   │   │   ├── ui/         # Jetpack Compose Screens
│   │   │   ├── data/       # Firebase & API integration
│   │   │   └── utils/      # Helper classes
├── screenshots/
│   ├── login_screen.png
│   ├── maps_screen.png
│   └── Publish_ride.png
```

---

## 🧾 Requirements

* Android Studio Bumblebee or newer
* Kotlin 1.7+ and Jetpack Compose setup
* Firebase Project
* Razorpay Developer Account
* Google Cloud Maps API Key

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 👤 Author

**Ginjupally Sainikhil**
📍 Hyderabad, India
📧 [ginjupallysainikhil@gmaiil.com](mailto:ginjuallysainikhil@gmail.com)
🔗 [LinkedIn](https://www.linkedin.com/in/ginjupallysainikhil/)


---

> *“Eco Ride – Drive Clean, Ride Green.”*

```

