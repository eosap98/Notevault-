# App Notepad

Aplikasi catatan aman dan terdesentralisasi dengan fitur enkripsi, kolaborasi real-time, dan penyimpanan data lokal maupun cloud via Firebase.

## Fitur Utama
- **Enkripsi Lokal**: Mengamankan catatan, daftar tugas, dan kata sandi menggunakan enkripsi perangkat yang canggih (Biometrik/PIN).
- **Kolaborasi Real-Time**: Obrolan aman dan sinkronisasi data antar perangkat dalam waktu nyata (menggunakan Firebase Firestore).
- **Notifikasi Terintegrasi**: Pemberitahuan pintar untuk pesan masuk, sesi kolaborasi, dan pembaruan brankas secara langsung.
- **Antarmuka Desentralisasi**: UI/UX modern, aman, dilengkapi berbagai tema kustom, animasi halus, dan pengalaman yang memukau.

## Persyaratan
- Android Studio Ladybug atau terbaru
- SDK Android minimal versi 26 (Android 8.0) rekomendasi target ke 34
- Akun Firebase (untuk Real-time database & Chat)

## Cara Setup / Pemasangan
Mengingat proyek ini akan diunggah ke repository baru Anda, cukup ikuti langkah pemasangan standar berikut:

1. Clone / Download repository ini (atau unduh format `.zip` lalu ekstrak).
2. Buka proyek ini menggunakan Android Studio (`File` > `Open` > Pilih folder `App Notepad`).
3. Hubungkan aplikasi ke layanan Firebase milik Anda (untuk fitur Obrolan & Kolaborasi):
   - Buka laman [Firebase Console](https://console.firebase.google.com/).
   - Buat proyek Firebase baru.
   - Tambahkan perangkat Android dengan nama paket `com.eostech.notepad`.
   - Unduh file konfigurasi `google-services.json` dan salin ke dalam folder `app/`.
   - Pastikan telah mengaktifkan **Firestore Database** di panel Firebase.
4. Lakukan sinkronisasi Gradle dengan menekan ikon Gajah (`Sync Project with Gradle Files`).
5. Jalankan aplikasi pada emulator atau ponsel langsung Anda.
