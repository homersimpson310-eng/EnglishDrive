# EnglishDrive – Cloud Build

Dieses Projekt baut automatisch eine Android-APK über GitHub Actions.

## Ohne Android Studio
1. ZIP entpacken.
2. Auf GitHub ein neues Repository namens `EnglishDrive` erstellen.
3. Alle Dateien und Ordner aus diesem Paket hochladen und committen.
4. Im Repository **Actions** öffnen.
5. **Build EnglishDrive APK** auswählen und **Run workflow** starten.
6. Nach dem Build unter **Artifacts** `EnglishDrive-debug-apk` herunterladen.
7. ZIP entpacken und `app-debug.apk` auf dem Android-Handy installieren.

Die APK ist zunächst eine Debug-Version. Die App enthält den aktuellen Lernprototyp mit Text-to-Speech, Spracherkennung und der Android-Auto-Media-Grundlage.
