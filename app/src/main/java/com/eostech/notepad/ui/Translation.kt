package com.eostech.notepad.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val indoToEngMap = mapOf(
    "Setelan" to "Settings",
    "Kolaborasi" to "Collaboration",
    "Tema & Tampilan" to "Theme & Display",
    "Pilih tema" to "Choose theme",
    "Personalisasi" to "Personalization",
    "Tema Kanvas: Doodle Art" to "Canvas Theme: Doodle Art",
    "Terapkan pola latar belakang coretan tangan halus ke ruang kerja Anda." to "Apply a subtle hand-drawn doodle background to your workspace.",
    "Bahasa / Language" to "Language",
    "Pilih bahasa aplikasi" to "Choose application language",
    "Keamanan" to "Security",
    "Kunci PIN Aplikasi" to "App PIN Lock",
    "Memerlukan PIN 6 digit untuk membuka aplikasi." to "Requires a 6-digit PIN to open the app.",
    "Data & Sinkronisasi" to "Data & Sync",
    "Sinkronisasi Firebase (Bawaan)" to "Firebase Sync",
    "Penyimpanan Cloud yang Aman" to "Secure Cloud Storage",
    "Hapus Kredensial Firebase" to "Clear Firebase Credentials",
    "Hanya gunakan jika Anda ingin mengganti server Firebase" to "Only use if you want to change the Firebase server",
    "Tentang" to "About",
    "Versi" to "Version",
    "Ganti PIN" to "Change PIN",
    "Kunci Biometrik" to "Biometric Lock",
    "Wajibkan sidik jari saat buka Aplikasi/Vault" to "Require fingerprint to open App/Vault",
    "Tangkapan & Rekaman Layar" to "Screenshot & Screen Recording",
    "Izinkan sistem merekam layar aplikasi" to "Allow capturing the app screen",
    "Dibuat oleh Eos Ageng" to "Made with love ❤️ by Eos Ageng",

    // UnifiedVault
    "Vault Bersama" to "Shared Vault",
    "Vault Pribadi" to "Private Vault",
    "Cari item..." to "Search items...",
    "Tidak ada catatan" to "No notes found",
    "Tidak ada daftar centang" to "No checklists found",
    "Tidak ada sandi tersimpan" to "No passwords saved",
    "Vault Anda kosong" to "Your vault is empty",
    "Ketuk tombol + untuk menambahkan item pertama Anda" to "Tap the + button to add your first item",
    "Tambah Catatan" to "Add Note",
    "Tambah Daftar" to "Add Checklist",
    "Tambah Sandi" to "Add Password",

    // Password
    "Konfirmasi Hapus" to "Confirm Delete",
    "Hapus" to "Delete",
    "Batal" to "Cancel",
    "Nama Pengguna" to "Username",
    "Sandi" to "Password",
    "Catatan" to "Notes",
    "Tambah Kredensial Baru" to "Add New Credential",
    "Edit Kredensial" to "Edit Credential",
    "Layanan (misal Netflix)" to "Service (e.g. Netflix)",
    "Nama Pengguna/Email" to "Username/Email",
    "Kata Sandi" to "Password",
    "Kategori" to "Category",
    "Kolom Tambahan" to "Additional Fields",
    "Nilai" to "Value",
    "Tambah Kolom Kustom" to "Add Custom Field",
    "Catatan Tambahan (Opsional)" to "Notes (Optional)",
    "Simpan Aman" to "Secure Save",
    "Umum" to "General",
    "Pribadi" to "Personal",
    "Pekerjaan" to "Work",
    "Keuangan" to "Finance",
    "Sosial" to "Social",

    // Chat
    "Chat" to "Chat",
    "Ketik pesan..." to "Type a message...",
    "Tidak ada vault" to "No vault",
    "Kirim" to "Send",
    "Belum terhubung dengan partner." to "Not connected to a partner.",
    "Belum ada pesan. Ucapkan halo!" to "No messages yet. Say hello!",

    // Checklist
    "Daftar Centang Baru" to "New Checklist",
    "Edit Daftar Centang" to "Edit Checklist",
    "Bagikan dengan Partner" to "Shared with Partner",
    "Hanya Pribadi" to "Personal Only",
    "Dibagikan" to "Shared",
    "Cari/tambahkan item..." to "Add item...",
    "Simpan" to "Save",
    "Judul Daftar" to "List Title",

    // Note Editor
    "Catatan Baru" to "New Note",
    "Mulai mengetik..." to "Start typing...",
    "Judul" to "Title",
    "Teks Baru" to "New Text",

    // Navigation and Others
    "Vault" to "Vault",
    "Bersama" to "Shared",
    "Sandi" to "Generator"
)

@Composable
fun String.tr(): String {
    val context = LocalContext.current
    val lang = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        .getString("language", "id") ?: "id"
    
    if (lang == "id") return this
    return indoToEngMap[this] ?: this // Fallback to original text if not found
}
