# App Notepad (Notevault)

Aplikasi catatan aman dan terdesentralisasi dengan fitur enkripsi, obrolan kolaborasi real-time, dan penyimpanan brankas rahasia secara mandiri via Firebase.

## Fitur Utama & Cara Penggunaan

Aplikasi ini memiliki beberapa fitur unggulan yang terbagi dalam berbagai menu produktivitas:

1. **Catatan (Notes) & Checklist**
   - **Catatan Format Penuh:** Anda bisa menyusun draf rapih dan membuat catatan keseharian dengan dukungan ragam format penulisan (Markdown Visual).
   - **Pengelola Tugas (Checklist):** Memudahkan Anda merekap seluruh rencana kegiatan atau daftar kebutuhan agar gampang diselesaikan.
   
2. **Brankas Rahasia (Unified Vault)**
   - **Proteksi Canggih:** Lupakan kekhawatiran karena semua data rahasia dilindungi oleh pin/segel biometrik. Enkripsi mandiri diberlakukan untuk kerahasiaan catatan maupun daftar *password* penting.
   - **Password Generator:** Anda juga dapat menggunakan perangkat cerdas pembuat kombinasi *password* tangguh hanya dengan satu kali klik.

3. **Obrolan & Kolaborasi antar Perangkat**
   - **Chatting Real-Time (Desentralisasi):** Jalin percakapan atau perbarui kolaborasi sinkron (tanpa delay). Pesan ini langsung ditangani oleh database Firebase pribadi.
   - **Notifikasi Latar Belakang:** Anda takkan ketinggalan peringatan pesan meski layar dalam keadaan mati, notifikasi *real-time* akan selalu menyelaraskan kolaborasi Anda.

---

## Panduan Custom Firebase Setup (Langsung dalam Aplikasi)

Sistem ini bersifat mandiri dan transparan (Decentralized Server). **Anda tidak perlu menggunakan Android Studio** atau membongkar kode aplikasi. 

Fitur Sinkronisasi dan Obrolan Kolaborasi di aplikasi bisa langsung digerakkan menggunakan Server *Firebase* pribadi Anda. Cukup lakukan pengaturan kilat berikut lalu masukkan izinnya di menu setelan aplikasi:

### Langkah 1: Buat Server Firebase Anda
1. Buka halaman [Firebase Console](https://console.firebase.google.com/) melalui browser di ponsel maupun PC Anda.
2. Klik tombol raksasa **"Create a Project"** (Buat proyek) lalu isikan nama unik (misal: *NotevaultServer*).
3. Anda bisa mematikan/melewatkan fitur *Google Analytics* jika tidak butuh analisis data. Lalu tekan **Create Project** (Buat Proyek).

### Langkah 2: Ambil Konfigurasi Aksesnya
Meskipun aplikasi ini berjalan di perangkat Android, kita cukup menggunakan metode termudah dengan mengambil profil kredensial API.
1. Begitu dashboard proyek terbuka, temukan dan tekan ikon melengkung **`</>` (Simbol Web)** di halaman tersebut untuk mendaftarkan wadah data.
2. Beri nama sekadar identifikasi (contoh: *Kunci API*). Lalu klik **Register App**.
3. Muncul kode *Script / Konfigurasi* (firebaseConfig). Anda cukup mencatat atau menyalin langsung tiga rincian utama ini sejenak:
   - `apiKey`
   - `projectId`
   - `appId`
4. Tekan *Continue to console*.

### Langkah 3: Aktifkan Wadah Chat (Firestore)
1. Dari menu sebelah kiri halaman Firebase, rentangkan menu **Build**, kemudian sentuh bagian **Firestore Database**.
2. Klik tombol tengah layar **Create database**.
3. (*Location*) Pilih letak benua terdekat supaya kecepatan real-timenya instan. Pilihannya misal `asia-southeast2` (Jakarta) jika menggunakan pengaturan regional Asia Pasifik.
4. **Tahap Paling Kritis:** Pada opsi *Aturan Keamanan / Security guidelines*, sangat wajib Anda memulainya dalam status Percobaan, yakni pilih opsi **"Start in test mode"**.
   - *Kenapa? Agar Aplikasi di handphone punya hak murni untuk mengatur isi pesan obrolan tanpa dibatalkan secara otomatis oleh firewall bawaan Firebase.*
5. Konfirmasi lalu tekan **Enable / Selesai**.

### Langkah 4: Masukkan di Aplikasi
1. Buka kembali aplikasi **App Notepad** yang sudah terpasang di handphone Anda.
2. Navigasi menuju halaman **Settings** (Setelan).
3. Sentuh menu bernama **"Custom Firebase Server"**.
4. Tempel barisan kunci otentikasi API yang telah kita kumpulkan dari Langkah 2 tadi (`API Key`, `Project ID`, dan `App ID`) ke masing-masing bidang khusus yang ada.
5. Klik tombol konfirmasi/simpan.

Selamat! Obrolan kolaborasi sepenuhnya ditangani secara otonom oleh pusat kendali lokal yang diimpor ke Firebase awan milik Anda sendiri. Bebas perantara, tanpa ada yang menyadap pesan kolaborator Anda!
