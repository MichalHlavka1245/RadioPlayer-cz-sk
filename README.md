Radio Player CZ/SK



## 🇬🇧 English Version

A mobile Android application designed for streaming Czech and Slovak radio stations. The project utilizes modern Android libraries (Kotlin) and the Firebase cloud platform for data and user management.

Key Features:
- Radio Streaming: Seamless background playback of selected radio station audio streams.
- Search Functionality: Fast real-time filtering of radio stations.
- Favorites: Ability to save favorite songs/stations linked directly to the user's account.
- Statistics: Tracking and storing listening metrics individually for each user.
- Authentication: Secure login via Google Account.

Architecture and Backend:
- Cloud Firestore: A NoSQL database used to fetch the radio station directory and store user statistics and favorites. The database is secured using strict Firestore Security Rules.
- Firebase Auth: User account management and Google Sign-In integration.

Instructions for the Teacher on How to Run the Project:

The project has been pre-configured to ensure the setup is as straightforward as possible, requiring no additional Firebase console configuration on your end.

1. **Clone the Repository:**
   ```bash
     git clone https://github.com/MichalHlavka1245/RadioPlayer-cz-sk.git

2. Open in Android Studio:
Open Android Studio, select Open, and choose the cloned project folder. Wait for the Gradle Sync to complete.

3. Signing Configuration (Important):
The repository includes a shared developer keystore located at app/debug.keystore. The build.gradle.kts file is configured to automatically use this keystore for the debug build. This ensures that Google Sign-In and Firebase integration will work out of the box without requiring you to generate a custom SHA-1 fingerprint.

4. Run the Application:
Connect a physical Android device (with USB Debugging enabled) or start an emulator (API 30+ recommended) and click the Run (Green arrow) button.

---

## 🇸🇰 Slovenská verzia

Mobilná Android aplikácia slúžiaca na streamovanie slovenských a českých rádio staníc. Projekt využíva moderné knižnice pre Android (Kotlin) a cloudovú platformu Firebase pre správu dát a používateľov.

### 🚀 Kľúčové funkcie
* **Streamovanie rádií:** Plynulé prehrávanie audio streamov zvolených staníc na pozadí.
* **Vyhľadávanie:** Rýchle filtrovanie rádio staníc v reálnom čase.
* **Obľúbené položky:** Možnosť ukladať si obľúbené skladby/stanice priradené k účtu.
* **Štatistiky:** Sledovanie a ukladanie štatistík počúvania pre každého používateľa samostatne.
* **Autentifikácia:** Bezpečné prihlasovanie cez Google účet.

### 🛠️ Architektúra a Backend
* **Cloud Firestore:** NoSQL databáza využívaná na načítavanie zoznamu rádií a ukladanie používateľských štatistík a obľúbených položiek. Databáza je zabezpečená pomocou prísnych pravidiel (Firestore Rules).
* **Firebase Auth:** Správa používateľských účtov a integrácia Google Sign-In.

---

### 📦 Pokyny na spustenie pre vyučujúceho

Projekt bol nakonfigurovaný tak, aby bolo jeho spustenie čo najjednoduchšie a nevyžadovalo žiadne dodatočné nastavovanie Firebase konzoly z Vašej strany.


1. **Klonovanie repozitára:**
   ```bash
     git clone https://github.com/MichalHlavka1245/RadioPlayer-cz-sk.git
   
3. **Otvorenie v Android Studio:**
Otvorte Android Studio, zvoľte Open a vyberte stiahnutý priečinok projektu. Počkajte na dokončenie synchronizácie zostavenia (Gradle Sync).

4. **Konfigurácia podpisu (Dôležité):**
Súčasťou repozitára je zdieľaný vývojársky kľúč app/debug.keystore. Súbor build.gradle.kts je nastavený tak, aby automaticky použil tento kľúč pre debug zostavu. Vďaka tomu Vám bude Google prihlasovanie a prepojenie s Firebase fungovať okamžite bez nutnosti generovať vlastný SHA-1 kľúč.

5. **Spustenie aplikácie:**
Pripojte fyzické Android zariadenie (so zapnutým USB Debuggingom) alebo spustite emulátor (odporúčané API 30+) a kliknite na tlačidlo Run (Zelená šípka).

 


 
 

  



