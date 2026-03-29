# App Notepad (Notevault)

Aplikasi catatan aman dan terdesentralisasi dengan fitur enkripsi, obrolan kolaborasi real-time, dan penyimpanan brankas rahasia secara lokal maupun cloud via Firebase.

## Fitur Utama & Cara Penggunaan

Aplikasi ini memiliki beberapa fitur unggulan yang terbagi dalam berbagai menu:

1. **Catatan (Notes) & Checklist**
   - **Pembuatan Catatan Lengkap:** Anda bisa membuat catatan harian dengan dukungan format huruf (tebal, miring, dll) menggunakan fitur penulisan interaktif (*VisualTransformation*).
   - **To-Do List/Checklist:** Memudahkan Anda menyusun daftar tugas atau belanjaan harian Anda yang bisa dipantau progresnya secara rapi.
   
2. **Brankas Rahasia (Unified Vault)**
   - **Enkripsi Lokal:** Seluruh data krusial dalam brankas diamankan dengan enkripsi super aman. Gunakan autentikasi Biometrik (Sidik Jari) atau PIN khusus yang sudah dikustomisasi untuk membukanya.
   - **Password Generator:** Anda juga dapat menggunakan generator pintar di dalam rincian keamanan untuk membuatkan kombinasi kata sandi super aman yang bisa disalin instan untuk keperluan registrasi.

3. **Obrolan & Kolaborasi (Terintegrasi)**
   - **Chatting Real-Time (Desentralisasi):** Jalin percakapan atau kolaborasi secara *real-time* dan responsif (tanpa delay). Semua obrolan ini didukung langsung oleh sistem Firebase.
   - **Sistem Notifikasi Pintar:** Dapatkan pemberitahuan Android asli (*push notification* latar belakang) kapanpun Anda mendapat pesan atau setiap ada pembaruan aktivitas khusus di proyek ini.

4. **Fitur Siluman & Penyamaran**
   - Aplikasi ini dilengkapi proteksi layar penyamaran ekstra *(Stealth Calculator)*. Cobalah ketikkan kombinasi rahasia `8888=` dari layar log in / setelan keamanan rahasia Anda untuk membuka kunci akses brankas terdalam.

---

## Panduan Setup Firebase & Koneksi Database

Agar fitur **Chat Real-Time** dan **Sinkronisasi Otomatis** berhasil berfungsi seutuhnya tanpa *force close* (error), Anda wajib mendaftarkan layanan dari Google Firebase sebagai backend database kita. Jangan lewatkan satu pun langkah berikut:

### 1. Memeriksa Proyek Lokal
1. Buka **Android Studio** -> Pilih *File* -> *Open* -> Cari dan pilih folder utama aplikasi (`App Notepad`).
2. Jangan mengganti Struktur atau Paket *Package Name* awal (`com.eostech.notepad`). Tunggu sesaat hingga proses pencarian indeks dan sinkronisasi `Gradle` awal selesai.

### 2. Membuat Server di Firebase
1. Buka website [Firebase Console](https://console.firebase.google.com/) melalui browser Anda (Login pakai akun Google Anda).
2. Klik besar-besar tombol **"Create a Project"** (Buat proyek) lalu ketikkan nama bebas seperti *NotevaultServer* atau *NotepadBackend*. Setujui persyaratan Firebase dan klik *Continue* hingga proyek selesai dirakit.

### 3. Tautkan Firebase ke Aplikasi Android Kita
1. Masuk ke halaman muka Dashboard Firebase (tepat setelah proyek terbuat), temukan ilustrasi *ikon Android* untuk menambahkan tipe aplikasi Android ➡️ Klik ikon tersebut.
2. Pada isian penting **Android package name** isi persis sesuai dengan sistem: 
   👉 `com.eostech.notepad`
3. Tekan persetujuan **Register app** (Daftarkan web/app).
4. Klik tombol simpan **Download `google-services.json`**.
5. Salin alias Copy *file json* yang baru saja Anda donwnload tersebut. Pindahkan ke dalam Android Studio persis di bagian / jalur `App Notepad/app/`. Atau seret dari folder download Anda menuju bagian menu tulisan `app` (Project Explorer) di samping kiri Android Studio Anda. Pastikan letaknya selevel dengan file `build.gradle.kts`.
6. Klik *Next* terus-menerus hingga Anda dialihkan kembali ke dasbor konsol *("Continue to console")*.

### 4. Aktifkan Database Utama (Firestore)
Fokus ke menu vertikal Firebase di sebelah kiri monitor Anda:
1. Klik dan rentangkan menu **Build** > Lalu pilih **Firestore Database**.
2. Sentuh tombol jingga **Create database**.
3. *Location*: Tentukan letak server basis data yang dekat untuk kecepatan real-time paling maksimal (misal wilayah Asia/Singapura: `asia-southeast1` atau Jakarta `asia-southeast2` jika tersedia).
4. *Rules / Aturan Keamanan*: Anda wajib  memilih memulainya dalam mode percoban alias opsi **"Start in Test Mode"**. Kenapa? Agar kode di dalam Android Studio bisa langsung diizinkan menulis dan membaca pesan chat tanpa halangan sertifikasi identitas rumit (*Bisa Anda perketat aturannya nanti via tab Rules saat aplikasi sudah 100% rilis publik).*
5. Klik selesai *Enable*.

Berhasil! Komponen backend obrolan *Firebase* kini sukses merespons kode di Android Anda!

### 5. Simulasikan Aplikasinya
1. Kembali buka Jendela Proyek dan Terminal **Android Studio**.
2. Pastikan sekali lagi Anda menyentuh tekan ikon logo Gajah (atau File > **Sync Project with Gradle Files**) supaya mesin pendeteksi plugin Firebase merangkum sinkronisasinya.
3. Sambungkan perangkat ponsel real via kabel data / Wireless debugging (Atau buka Emulator Virtual). 
4. Pencet *ikon putar Segitiga (Run)* berwarna Hijau - dan voila! Aplikasi Notepad super canggih Anda akan otomatis merespons notifikasi dan percakapan seketika di peranti Anda!
