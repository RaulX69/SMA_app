# Match Journal App - Full-Stack Android Project

O aplicație mobilă completă (Frontend + Backend) dezvoltată pentru pasionații de fotbal, care permite căutarea meciurilor reale și crearea unui jurnal personalizat. Proiectul demonstrează implementarea unui flux CRUD complet, comunicarea cu un API extern (API-Football) și integrarea senzorilor hardware ai telefonului (Cameră și GPS).

## Funcționalități principale
* **Autentificare Securizată:** Sistem de Login și Înregistrare bazat pe token-uri.
* **Căutare Meciuri (API Extern):** Integrare cu API-Football pentru a căuta echipe și a prelua date reale (echipe, scor, dată, stadion).
* **Jurnal CRUD Complet:**
    * **Create:** Salvarea automată a meciurilor preferate în baza de date proprie din rezultatele căutării.
    * **Read:** Dashboard interactiv care afișează istoricul meciurilor salvate de utilizator.
    * **Update (Senzori & UI):** Editarea meciurilor pentru a adăuga o notă (1-5), o recenzie personală, o **Fotografie** (Cameră) și coordonatele **GPS** ale locației.
    * **Delete:** Ștergerea meciurilor din jurnal cu fereastră de confirmare (AlertDialog).

## Tehnologii folosite
**Frontend (Android App):**
* **Limbaj:** Kotlin
* **UI:** Jetpack Compose
* **Rețea:** Retrofit2 & Gson pentru HTTP requests
* **Asincronism:** Coroutines
* **Arhitectură:** Navigation Compose (Single-Activity Architecture)
* **Senzori Hardware:** Camera API, Location Services (GPS)

**Backend & Bază de Date:**
* **Limbaj & Framework:** Node.js, Express.js
* **Bază de date:** PostgreSQL
* **Altele:** Axios (pentru API-Football), JWT (Autentificare)