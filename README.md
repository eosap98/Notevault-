# App Notepad (Notevault)

Aplikasi catatan super aman dengan fitur obrolan (chat) dan sinkronisasi data antar perangkat. Semua data rahasia diamankan dengan sidik jari atau PIN. Kerennya, Anda bisa menggunakan server *Firebase* milik Anda sendiri agar obrolan berjalan lancar tanpa campur tangan orang lain.

## Fitur Utama Aplikasi

1. **Catatan Terformat & Daftar Tugas:** Buat catatan harian atau checklist kerjaan dengan sangat mudah dan rapi.
2. **Brankas Pribadi (Vault):** File dan teks rahasia Anda akan dikunci ketat menggunakan PIN dan Biometrik (Sidik Jari).
3. **Password Generator:** Pembuat kata sandi pintar yang langsung mengacak kombinasi sandi susah ditebak dengan satu ketukan.
4. **Obrolan Cepat Kapan Saja:** Fitur *Chatting* yang responsif dan mendukung sinkronisasi seketika antarpengguna.

---

## Tutorial Menghubungkan Aplikasi ke Server Firebase Sendiri

Agar fitur **Chat** dan **Sinkronisasi antar-HP** bisa menyala total, Anda cuma mengatur pengaturan server Firebase pribadi dan memasukkan nilainya ke setelan aplikasi. Jangan khawatir, caranya sangat mudah dipahami! Ikuti panduannya perlahan:

### Tahap 1: Bikin Ruangan Proyek di Firebase
1. Buka laman [Firebase Console](https://console.firebase.google.com/) lewat *browser* di HP atau Laptop Anda.
2. Temukan dan klik tombol besar **Buat Proyek (Create a Project)**.
3. Cukup isikan nama proyek sesuka hati Anda (contoh: *ServerNotepadKu*).
4. Agar lebih gampang, Anda bisa mematikan saja sakelar opsi *Google Analytics*, lalu klik **Create Project** (Buat Proyek).

### Tahap 2: Catat Kunci Konfigurasi API Nya
Untuk bisa terhubung, kita butuh "Surat Izin" masuk.
1. Sesampainya di halaman awal Firebase, cari tombol bundar bergambar tombol kurung sudut putih bertuliskan **`</>` (ikon Web)**, lalu klik ikon tersebut.
2. Ketik nama bebas untuk pendaftarannya (misal: *IzinAplikasi*), dan klik **Register App** (Daftarkan Aplikasi).
3. Firebase akan memunculkan sebuah kotak *Script* berwarna abu-abu panjang. **Tugas Anda hanya perlu menyalin (Copy) 3 hal ini dari kotak itu:**
   - Baris tulisan kode `apiKey`
   - Baris tulisan nama `projectId`
   - Baris tulisan kode `appId`
4. Tekan tombol **Continue to console** jika sudah menyalin kuncinya.

### Tahap 3: Nyalakan Realtime Database & Salin "Database URL"
Data obrolan akan difasilitasi oleh fitur bernama `Realtime Database`.
1. Tengok bagian kiri halaman utama Firebase (Jika Anda di HP, klik garis tiga buka pilihan).
2. Rentangkan opsi tulisan **Build**, kemudian barulah klik opsi **Realtime Database**.
3. Sentuh tombol jingga bertuliskan **Create Database**.
4. Biarkan atau pilih lokasinya (Misal lokasi *Singapore* biar cepat) lalu klik tombol *Next*.
5. **Ini Sangat Penting:** Anda akan ditanya masalah izin keamanan, ubah pilihan dari terkunci "*Locked Mode*" menjadi "*Mode Uji Coba*" yaitu pilih tulisan bundar **"Start in test mode"**. Lalu klik tombol **Enable** (Selesai).
6. Tampilan memori telah jadi. Tepat di bagian atas memori yang berkedip, Anda akan melihat sebuah tautan tulisan web (**link tebal** berwarna kuning kecoklatan) berhuruf kecil semua. Bentuknya berupa: `https://namaproyek-default-rtdb.asia-southeast1.firebasedatabase.app/`
7. **Silakan Salin penuh link terpanjang tersebut**. Ini namanya **Database URL**.

### Tahap 4: Saatnya Masukkan Seluruh Kunci ke Aplikasi!
Pusat kontrol server Firebase Anda sudah terbangun kuat. Sekarang gabungkan profilnya ke HP Anda!
1. Buka dan jalankan kembali **App Notepad (Notevault)** di Handphone pribadi Anda.
2. Menuju halaman menu **Setelan (Settings)** aplikasi.
3. Pilih kolom bernama **Custom Firebase Server Setup**.
4. Tempel (*Paste*) semua isian yang sudah sukses dikumpulkan di tahap tadi satu per satu secara teliti:
   - ✅ **API Key**
   - ✅ **Project ID**
   - ✅ **App ID**
   - ✅ Lengkap beserta **Database URL**
5. Pencet tombol simpan.

Mantap! Jika seluruh kuncinya tepat, fitur komunikasi *Chat* di HP Anda telah mekar menjadi jaringan independen tanpa ada pihak lain yang menonton obrolan dan dokumen Anda. Semuanya beroperasi penuh dengan sempurna.
