package com.eostech.notepad.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PinManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setPin(pin: String) {
        if (pin.length == 6) {
            prefs.edit().putString(KEY_PIN, pin).apply()
        }
    }

    fun getPin(): String? {
        return prefs.getString(KEY_PIN, null)
    }

    fun isPinSet(): Boolean {
        return getPin() != null
    }

    fun verifyPin(input: String): Boolean {
        return getPin() == input
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    companion object {
        private const val KEY_PIN = "app_pin_code"
    }
}
