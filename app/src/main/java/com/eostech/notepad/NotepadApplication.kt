package com.eostech.notepad

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class NotepadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    fun initFirebase() {
        val prefs = getSharedPreferences("firebase_config", MODE_PRIVATE)
        val customProjectId = prefs.getString("project_id", "").orEmpty().trim()
        val customApiKey   = prefs.getString("api_key",    "").orEmpty().trim()
        val customAppId    = prefs.getString("app_id",     "").orEmpty().trim()
        val customDbUrl    = prefs.getString("db_url",     "").orEmpty().trim()

        val hasCustomConfig = customProjectId.isNotEmpty()
                && customApiKey.isNotEmpty()
                && customAppId.isNotEmpty()
                && customDbUrl.isNotEmpty()

        if (hasCustomConfig) {
            try {
                val options = FirebaseOptions.Builder()
                    .setProjectId(customProjectId)
                    .setApiKey(customApiKey)
                    .setApplicationId(customAppId)
                    .setStorageBucket("$customProjectId.appspot.com")
                    .setDatabaseUrl(customDbUrl)
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("NotepadApp", "Firebase initialized with custom config: $customProjectId")
            } catch (e: Exception) {
                Log.e("NotepadApp", "Failed to init custom Firebase: ${e.message}")
            }
        } else {
            // Tidak ada Firebase default — pengguna harus setup sendiri
            Log.w("NotepadApp", "No Firebase config found. Features requiring Firebase will be disabled.")
        }
    }

    companion object {
        fun isFirebaseReady(): Boolean {
            return try {
                FirebaseApp.getInstance()
                true
            } catch (e: IllegalStateException) {
                false
            }
        }
    }
}
