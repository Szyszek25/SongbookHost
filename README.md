# SongbookHost Android

Minimalny projekt Android (Kotlin) tworzący lokalny hotspot Wi‑Fi i
serwer HTTP/WebSocket (Ktor) udostępniający teksty piosenek offline.

## Szybki start
1. Otwórz folder `SongbookHost` w **Android Studio 2022.3+**.  
2. Zaczekaj na synchronizację Gradle, ewentualnie zaakceptuj aktualizację pluginów.  
3. Uruchom aplikację na urządzeniu **z Androidem 8.0 (API 26)+**.  
4. Naciśnij **Uruchom hotspot** – pojawi się SSID i hasło w pasku systemowym.  
5. Inne telefony mogą podłączyć się do tego AP, a w przeglądarce wejść na  
   `http://192.168.43.1:8080/songs/1` (adres IP hosta może się różnić).  

### Dalszy rozwój
- Dodaj listę piosenek w `assets/songs`.  
- Rozbuduj UI na klientach (HTML/JS lub aplikacja).  
- Implementuj broadcast tekstu / karaoke via WebSocket.  
