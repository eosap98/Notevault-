package com.eostech.notepad.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import android.util.Log

private val Context.dataStore by preferencesDataStore(name = "settings")

class CollaborationManager(private val context: Context) {
    private val firestore get() = FirebaseFirestore.getInstance()
    private val PARTNER_ID = stringPreferencesKey("partner_id")
    private val VAULT_ID = stringPreferencesKey("vault_id")
    private val HOST_TOKEN = stringPreferencesKey("host_token")
    private val LOCAL_USER_ID = stringPreferencesKey("local_user_id")

    val partnerId: Flow<String?> = context.dataStore.data.map { it[PARTNER_ID] }
    val vaultId: Flow<String?> = context.dataStore.data.map { it[VAULT_ID] }
    val hostToken: Flow<String?> = context.dataStore.data.map { it[HOST_TOKEN] }
    val localUserId: Flow<String> = context.dataStore.data.map { 
        it[LOCAL_USER_ID] ?: "user_loading"
    }

    suspend fun getOrCreateLocalUserId(): String {
        val currentId = context.dataStore.data.map { it[LOCAL_USER_ID] }.first()
        if (!currentId.isNullOrEmpty()) return currentId
        
        val newId = "user_${java.util.UUID.randomUUID().toString().take(8)}"
        context.dataStore.edit {
            it[LOCAL_USER_ID] = newId
        }
        return newId
    }

    suspend fun generatePairingCode(userId: String): String {
        return try {
            val prefs = context.getSharedPreferences("firebase_config", Context.MODE_PRIVATE)
            val p = prefs.getString("project_id", "") ?: ""
            val k = prefs.getString("api_key", "") ?: ""
            val a = prefs.getString("app_id", "") ?: ""
            val d = prefs.getString("db_url", "") ?: ""
            
            val newVaultId = "vault_${userId}_${java.util.UUID.randomUUID().toString().take(8)}"
            
            val json = org.json.JSONObject()
            json.put("p", p)
            json.put("k", k)
            json.put("a", a)
            json.put("d", d)
            json.put("v", newVaultId)
            json.put("u", userId)
            
            val token = android.util.Base64.encodeToString(json.toString().toByteArray(), android.util.Base64.NO_WRAP)
            
            // Initialize vault status in Firestore
            firestore.collection("vaults").document(newVaultId).set(mapOf("status" to "active"))
            
            // Save vaultId and partnerId for HOST
            context.dataStore.edit {
                it[PARTNER_ID] = "client_connected"
                it[VAULT_ID] = newVaultId
                it[HOST_TOKEN] = token
            }
            
            Log.d("CollaborationManager", "Magic Token generated for vault $newVaultId")
            token
        } catch (e: Exception) {
            Log.e("CollaborationManager", "Error generating magic token: ${e.message}", e)
            throw e
        }
    }

    suspend fun joinVault(userId: String, token: String): Boolean {
        return try {
            Log.d("CollaborationManager", "Joining vault with Magic Token")
            val decodedStr = String(android.util.Base64.decode(token, android.util.Base64.DEFAULT))
            val json = org.json.JSONObject(decodedStr)
            
            val p = json.getString("p")
            val k = json.getString("k")
            val a = json.getString("a")
            val d = json.getString("d")
            val v = json.getString("v")
            val u = json.getString("u")
            
            // Save Firebase config for CLIENT
            val prefs = context.getSharedPreferences("firebase_config", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("project_id", p)
                .putString("api_key", k)
                .putString("app_id", a)
                .putString("db_url", d)
                .apply()
            
            // Save vaultId and partnerId for CLIENT
            context.dataStore.edit {
                it[PARTNER_ID] = u
                it[VAULT_ID] = v
            }
            
            Log.d("CollaborationManager", "Successfully joined vault $v")
            true
        } catch (e: Exception) {
            Log.e("CollaborationManager", "Error joining with token: ${e.message}", e)
            false
        }
    }

    suspend fun disconnect() {
        val vid = context.dataStore.data.map { it[VAULT_ID] }.firstOrNull()
        if (vid != null) {
            try {
                firestore.collection("vaults").document(vid).update("status", "ended")
            } catch (e: Exception) {
                Log.e("CollaborationManager", "Error updating vault status: ${e.message}")
            }
        }
        
        context.dataStore.edit {
            it.remove(PARTNER_ID)
            it.remove(VAULT_ID)
            it.remove(HOST_TOKEN)
        }
    }
}
