# App Notepad (Notevault)

Aplikasi catatan super aman dengan fitur obrolan (chat) dan sinkronisasi data antar perangkat. Semua data rahasia diamankan dengan sidik jari atau PIN. Kerennya, Anda bisa menggunakan server *Firebase* milik Anda sendiri agar obrolan berjalan lancar tanpa campur tangan orang lain.

## Fitur Utama Aplikasi

1. **Catatan Terformat & Daftar Tugas:** Buat catatan harian atau checklist kerjaan dengan sangat mudah dan rapi.
2. **Brankas Pribadi (Vault):** File dan teks rahasia Anda akan dikunci ketat menggunakan PIN dan Biometrik (Sidik Jari).
3. **Password Generator:** Pembuat kata sandi pintar yang langsung mengacak kombinasi sandi susah ditebak dengan satu ketukan.
4. **Obrolan Cepat Kapan Saja:** Fitur *Chatting* yang responsif dan mendukung sinkronisasi seketika antarpengguna.

---

## Panduan Lengkap: Cara Setup Firebase untuk App Notepad

Agar Anda dan teman Anda dapat menggunakan fitur kolaborasi (Catatan, Checklist, Password) dan **Chat** secara mulus, Anda harus membuat server *Cloud* Anda sendiri menggunakan Firebase secara **gratis**. 

Ikuti langkah-langkah di bawah ini secara teliti dari awal hingga akhir.

---

### TAHAP 1: Membuat Proyek Firebase
1. Buka browser di komputer/HP Anda dan pergi ke [Firebase Console](https://console.firebase.google.com/).
2. Login menggunakan akun Google Anda.
3. Klik tombol **Create a Project** (Buat Proyek).
4. Masukkan nama proyek (misalnya: `NotepadServerku`).
5. Matikan (Disable) *Google Analytics* jika tidak diperlukan agar lebih cepat, lalu klik **Create Project**.

### TAHAP 2: Mendaftarkan Aplikasi Android
1. Setelah proyek siap, di halaman beranda Firebase, klik ikon **Android** (logo robot hijau) untuk menambahkan aplikasi.
2. Di kolom **Android package name**, masukkan wajib persis seperti ini: `com.eostech.notepad`.
3. Biarkan sisa kolom kosong, lalu klik **Register app**.
4. Abaikan langkah *Download google-services.json*, langsung klik **Next** terus menerus hingga **Continue to console**.

### TAHAP 3: Mendapatkan Kode Kredensial
Kini Anda harus menyalin (*copy*) 3 kode penting untuk dimasukkan ke aplikasi nanti.
1. Di panel kiri atas, klik **ikon Gerigi (⚙️)** di sebelah tulisan *Project Overview*, lalu pilih **Project settings**.
2. Pada tab *General* (Umum), cari dan salin (*copy*):
   - **Project ID** (contoh: `notepadserverku-a1b2`)
   - **Web API Key** (Sederetan teks panjang yang berawalan `AIzaSy...`)
3. Gulir ke bagian paling bawah ke kotak **"Your apps"**.
4. Salin **App ID** (formatnya panjang, dimulai dengan angka 1, contoh: `1:1234567890:android:abc...`).

### TAHAP 4: Mengaktifkan Database (SANGAT PENTING)
Aplikasi ini membutuhkan **dua** jenis konfigurasi *database* agar seluruh fitur Sinkronisasi dan *Chat* berjalan tanpa henti.

#### A. Membuat Realtime Database (Untuk URL)
1. Di panel kiri Firebase, lihat bagian **Build**, lalu pilih **Realtime Database**.
2. Klik tombol **Create Database**.
3. **Pilih Lokasi Bebas** (Kini Anda bebas memilih United States, Belgium, atau Singapore). Klik Next.
4. Pilih **Test Mode**, lalu klik Enable.
5. Anda akan melihat tautan URL besar (contoh: `https://notepadserverku-default-rtdb.firebaseio.com/`). **SALIN URL PENUH INI**. Ini adalah `Database URL` Anda.

#### B. Membuat Cloud Firestore (Untuk Chat & Sinkronisasi Utama)
*App Notepad ini secara teknis menyimpan seluruh teks catatan, obrolan, dan password di platform Cloud Firestore.*
1. Masih di panel kiri (**Build**), pilih **Firestore Database**.
2. Klik **Create database**.
3. Pilih lokasi server yang sama atau berdekatan, lalu pilih **Start in Test Mode**.
4. Klik **Create**.
5. Tunggu hingga database selesai dibuat. Setelah tampil, pastikan tab **Rules** memperlihatkan keterangan *allow read, write: if true;*.

---

### TAHAP 5: Memasukkan Pengaturan ke Aplikasi
Sekarang atur semuanya di dalam aplikasi App Notepad Anda:

1. Buka **App Notepad**.
2. Masuk ke menu **Pengaturan** (Settings).
3. Gulir ke bawah hingga bagian **Firebase Setup**.
4. Buka formulir pengaturan, lalu Isi kolom sesuai dengan data yang Anda salin dari Firebase:
   - **Project ID:** (Tempel *Project ID* dari Tahap 3)
   - **API Key:** (Tempel *Web API Key* berawalan AIzaSy)
   - **App ID:** (Tempel *App ID* Android)
   - **Database URL Lengkap:** (Tempel Link Realtime Database dari Tahap 4A, pastikan **dimulai dengan https://**)
5. Klik **Simpan & Terapkan**.
6. **MATIKAN PAKSA (Force Close)** aplikasi dari *recent apps* Android Anda.
7. Buka ulang App Notepad.

Selamat! Kini aplikasi Anda telah memiliki server sendiri yang bebas batasan! Anda tinggal masuk ke bagian Kolaborasi, buat Kode Pairing, dan bagikan kode tersebut ke perangkat teman Anda yang juga telah dimodifikasi pengaturannya dengan kode Firebase Anda!
