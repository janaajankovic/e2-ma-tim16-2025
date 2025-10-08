## Uputstvo za pokretanje aplikacije

Da biste pokrenuli projekat lokalno, pratite sledeće korake.

### Preduslovi

* Instaliran **Android Studio** (preporučeno najnovija verzija).
* **Firebase nalog** (besplatan).

### 1. Firebase podešavanje (Najvažniji korak)

Aplikacija ne može da radi bez Firebase-a. Potrebno je da je povežete sa vašim Firebase projektom.

1.  Idite na [Firebase Console](https://console.firebase.google.com/).
2.  Kreirajte novi projekat (npr. "HabitTrackerRPG-Test").
3.  Unutar projekta, kliknite na **"Add app"** i izaberite **Android** ikonicu.
4.  U polje "Android package name" unesite tačno: **`com.example.habittrackerrpg`**.
5.  Preuzmite fajl **`google-services.json`** koji će vam Firebase ponuditi.
6.  Prevucite taj fajl u vaš Android Studio projekat, u folder **`app/`**.
7.  U Firebase konzoli, u meniju sa leve strane, idite na **Build -> Firestore Database**.
    * Kliknite na "Create database".
    * Izaberite **Start in test mode**. Ovo je važno da bi aplikacija mogla da piše u bazu bez komplikovanih pravila.
8.  U Firebase konzoli, idite na **Build -> Authentication**.
    * Idite na tab "Sign-in method".
    * Omogućite (Enable) **Email/Password** kao metodu prijavljivanja.

### 2. Pokretanje u Android Studiju

1.  Otvorite projekat u Android Studiju.
2.  Sačekajte da Gradle završi sinhronizaciju (može potrajati nekoliko minuta prvi put).
3.  Povežite fizički Android uređaj (sa uključenim USB Debugging) ili pokrenite emulator.
4.  Izaberite uređaj u gornjem meniju Android Studija.
5.  Kliknite na zelenu "Play" ikonicu (▶️ Run 'app').

Aplikacija će se instalirati i pokrenuti na vašem uređaju/emulatoru.
