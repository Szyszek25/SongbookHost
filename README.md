# SongbookHost Android

Aplikacja Android umożliwiająca włączenie lokalnego hotspotu Wi‑Fi i udostępnianie tekstów piosenek przez serwer Ktor (REST + WebSocket). Całość działa offline na jednym urządzeniu‑hoście.

## Szybki start
1. Otwórz folder `SongbookHost` w **Android Studio 2022.3+**.
2. Po synchronizacji Gradle uruchom aplikację na urządzeniu **z Androidem 8.0 (API 26)+**.
3. Naciśnij **Uruchom hotspot** – pojawi się adres IP hosta.
4. Inne telefony mogą podłączyć się do tego AP i w przeglądarce wejść na `http://&lt;IP hosta&gt;:8080/songs/1`.

### Dalszy rozwój
- Dodaj więcej utworów w `assets/songs`.
- Rozbuduj UI klientów (HTML/JS lub aplikacja).
