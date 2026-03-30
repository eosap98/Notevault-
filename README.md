# App Notepad (Notevault)

Aplikasi catatan AMAN dengan fitur obrolan (chat) menggunakan *firebase* dan sinkronisasi data antar perangkat. Semua data rahasia diamankan dengan sidik jari atau PIN.

## Fitur Utama Aplikasi

1. **Catatan Terformat & Daftar Tugas:** Buat catatan harian atau checklist kerjaan dengan sangat mudah dan rapi.
2. **Brankas Pribadi (Vault):** teks rahasia akan dikunci menggunakan PIN dan Biometrik (Sidik Jari).
3. **Password Generator:** Pembuat kata sandi pintar yang langsung mengacak kombinasi sandi susah ditebak.
4. **Obrolan Cepat Kapan Saja:** Fitur *Chatting* yang responsif dan mendukung sinkronisasi seketika antarpengguna.

---

## Panduan Lengkap: Cara Setup Firebase untuk App Notepad

Agar dapat menggunakan fitur kolaborasi (Catatan, Checklist, Password) dan **Chat** , Anda harus membuat server *Cloud* menggunakan Firebase secara **gratis**. 

Ikuti langkah-langkah di bawah ini secara teliti dari awal hingga akhir.

---

### TAHAP 1: Membuat Proyek Firebase
1. Buka browser di komputer/HP dan masuk ke [Firebase Console](https://console.firebase.google.com/).
2. Login menggunakan akun Google.
3. Klik tombol **Create a Project** (Buat Proyek).
4. Masukkan nama proyek bebas (misalnya: `NotepadServerku`).
5. Matikan (Disable) *Google Analytics* lalu klik **Create Project**.

### TAHAP 2: Mendaftarkan Jalur Aplikasi
Karena setup dan integrasi Firebase dilakukan secara manual *langsung* dari dalam aplikasi lewat *Setelan*, Anda **TIDAK PERLU** lagi menautkan atau mendownload file pendukung seperti `google-services.json` atau mendaftarkan versi Android-nya.

Prosesnya dapat diringkas cukup lewat platform *Web* saja agar kuncinya cepat muncul:
1. Di beranda (*Project Overview*) Firebase Anda, klik tombol **Add app**, lalu pilih langsung ikon **WEB (`</>`)**.
2. Di bagian form **App nickname**, isi dengan nama bebas (misalnya: `NotevaultAkses`).
3. Klik tombol **Register app**.

### TAHAP 3: Mendapatkan Kode Kredensial
Setelah menekan tombol *Register app* pada tahap 2 tadi, sistem akan seketika menghasilkan sebuah kotak yang berisi tulisan-tulisan kode (*firebaseConfig*).
*NOTE: Ini sekaligus menjawab kebingungan jika di masa lalu Web API Key sering tidak terlihat atau disembunyikan di konsol General.*
1. Lirik ke dalam kotak kode tersebut. Anda wajib melakukan **Salin (copy)** tepat pada tiga rincian utama ini:
   - Baris teks setelah **`apiKey`** (Ini adalah *Web API Key* Anda).
   - Baris teks setelah **`projectId`** (Ini adalah *Project ID* Anda).
   - Baris teks setelah **`appId`** (Ini adalah *App ID* Anda).
2. Sesudah menyalin ketiganya dengan rapi, silakan klik tombol putih **Continue to console** di sebelah bawah kotak.


### TAHAP 4: Mengaktifkan Database (SANGAT PENTING)
Aplikasi ini membutuhkan **dua** jenis konfigurasi *database* agar seluruh fitur Sinkronisasi dan *Chat* bekerja.

#### A. Membuat Realtime Database (Untuk URL)
1. Di panel kiri Firebase bagian **Databases & Storage**, lalu pilih **Realtime Database**.
2. Klik tombol **Create Database**.
3. **Pilih Lokasi Bebas**
4. Pilih **Test Mode**, lalu Enable.
5. akan muncul tautan URL (contoh: `https://notepadserverku-default-rtzsdb.firebaseio.com/`). **SALIN URL PENUH INI**. Ini adalah `Database URL` Anda.

#### B. Membuat Cloud Firestore (Untuk Chat & Sinkronisasi Utama)
*App Notepad ini secara teknis menyimpan seluruh teks catatan, obrolan, dan password di platform Cloud Firestore.*
1. Masih di panel kiri (**Databases & Storage**), pilih **Firestore**.
2. Klik **Create database**.
3. Pilih lokasi server yang sama dengan **Realtime Database**, lalu pilih **Start in Production Mode**.
4. Klik **Create**.
5. Tunggu hingga database selesai dibuat. Setelah tampil, pastikan tab **Rules** memperlihatkan keterangan *allow read, write: if true;*.

---

### TAHAP 5: Memasukkan Pengaturan ke Aplikasi
Sekarang atur semuanya di dalam aplikasi:

1. Buka **Notevault**.
2. Masuk ke menu **Pengaturan** (Settings).
3. ke bagian **Firebase Setup**.
4. Isi kolom sesuai dengan data yang Anda salin dari Firebase:
   - **Project ID:** (Tempel *Project ID* dari Tahap 3)
   - **API Key:** (Tempel *Web API Key* berawalan AIzaSy)
   - **App ID:** (Tempel *App ID* Android)
   - **Database URL:** (Tempel Link Realtime Database dari Tahap 4A, pastikan **dimulai dengan https://**)
5. Klik **Simpan & Terapkan**.
6. lalu Close aplikasi dari *recent apps* dan buka ulang Aplikasi
7. masuk ke setelan di bagian Kolaborasi, buat Kode Pairing, dan bagikan kode tersebut ke perangkat teman.

