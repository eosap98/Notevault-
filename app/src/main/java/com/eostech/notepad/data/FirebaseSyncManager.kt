package com.eostech.notepad.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.eostech.notepad.util.NotificationHelper
import com.google.firebase.firestore.DocumentChange

class FirebaseSyncManager(private val context: Context, private val database: AppDatabase) {

    private val firestore get() = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val notificationHelper = NotificationHelper(context)
    private var currentUserId: String = "user_placeholder"
    private var noteListener: ListenerRegistration? = null
    private var checklistListener: ListenerRegistration? = null
    private var passwordListener: ListenerRegistration? = null

    fun startSync(userId: String) {
        this.currentUserId = userId
        stopSync()

        // Sync Notes
        noteListener = firestore.collection("users").document(userId).collection("notes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                // Track if this is the initial load to avoid backfilling notifications
                val isInitial = snapshots?.metadata?.hasPendingWrites() == false && snapshots.metadata.isFromCache
                
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val title = doc.getString("title") ?: "Tanpa Judul"
                        val remoteLastModified = doc.getLong("lastModified") ?: 0L
                        
                        // Show notification if it's truly new (within last 5 seconds)
                        if (System.currentTimeMillis() - remoteLastModified < 5000) {
                            notificationHelper.showNotification("Catatan Baru", "Partner membagikan catatan: $title")
                        }
                        
                        scope.launch {
                            val localNote = database.noteDao().getNoteByFirebaseId(doc.id)
                            if (localNote == null || remoteLastModified > localNote.lastModified) {
                                val note = NoteEntity(
                                    id = localNote?.id ?: 0,
                                    title = title,
                                    content = doc.getString("content") ?: "",
                                    tags = doc.getString("tags") ?: "",
                                    color = doc.getLong("color")?.toInt() ?: 0,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isShared = true,
                                    firebaseId = doc.id,
                                    lastModified = remoteLastModified
                                )
                                database.noteDao().insertNote(note)
                            }
                        }
                    } else if (change.type == DocumentChange.Type.MODIFIED) {
                        val doc = change.document
                        val remoteLastModified = doc.getLong("lastModified") ?: 0L
                        scope.launch {
                            val localNote = database.noteDao().getNoteByFirebaseId(doc.id)
                            if (localNote != null && remoteLastModified > localNote.lastModified) {
                                val note = localNote.copy(
                                    title = doc.getString("title") ?: localNote.title,
                                    content = doc.getString("content") ?: localNote.content,
                                    lastModified = remoteLastModified
                                )
                                database.noteDao().insertNote(note)
                            }
                        }
                    }
                }
            }

        // Sync Checklists & Items
        checklistListener = firestore.collection("users").document(userId).collection("checklists")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val title = doc.getString("title") ?: "Tanpa Judul"
                        val remoteLastModified = doc.getLong("lastModified") ?: 0L
                        
                        if (System.currentTimeMillis() - remoteLastModified < 5000) {
                            notificationHelper.showNotification("Daftar Baru", "Partner membagikan daftar centang: $title")
                        }

                        scope.launch {
                            val localChecklist = database.checklistDao().getChecklistByFirebaseId(doc.id)
                            if (localChecklist == null || remoteLastModified > localChecklist.lastModified) {
                                val checklist = ChecklistEntity(
                                    id = localChecklist?.id ?: 0,
                                    title = title,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isShared = true,
                                    firebaseId = doc.id,
                                    lastModified = remoteLastModified
                                )
                                val cid = database.checklistDao().insertChecklist(checklist)
                                syncChecklistItems(userId, doc.id, cid)
                            }
                        }
                    }
                }
            }

        // Sync Passwords
        passwordListener = firestore.collection("users").document(userId).collection("passwords")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val service = doc.getString("serviceName") ?: "Tanpa Nama"
                        val remoteLastModified = doc.getLong("lastModified") ?: 0L
                        
                        if (System.currentTimeMillis() - remoteLastModified < 5000) {
                            notificationHelper.showNotification("Sandi Baru", "Partner membagikan sandi untuk: $service")
                        }

                        scope.launch {
                            val localPassword = database.passwordDao().getPasswordByFirebaseId(doc.id)
                            if (localPassword == null || remoteLastModified > localPassword.lastModified) {
                                val password = PasswordEntity(
                                    id = localPassword?.id ?: 0,
                                    serviceName = service,
                                    username = doc.getString("username") ?: "",
                                    encryptedPassword = doc.getString("encryptedPassword") ?: "",
                                    iv = doc.getString("iv") ?: "",
                                    notes = doc.getString("notes") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isShared = true,
                                    firebaseId = doc.id,
                                    lastModified = remoteLastModified,
                                    customFields = doc.getString("customFields") ?: "[]",
                                    logoUrl = doc.getString("logoUrl")
                                )
                                database.passwordDao().insertPassword(password)
                            }
                        }
                    }
                }
            }
    }

    private fun syncChecklistItems(userId: String, firebaseChecklistId: String, localChecklistId: Long) {
        firestore.collection("users").document(userId)
            .collection("checklists").document(firebaseChecklistId)
            .collection("items")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                snapshots?.documents?.forEach { doc ->
                    val remoteLastModified = doc.getLong("lastModified") ?: 0L
                    scope.launch {
                        val localItem = database.checklistDao().getItemByFirebaseId(doc.id)
                        if (localItem == null || remoteLastModified > localItem.lastModified) {
                            val item = ChecklistItemEntity(
                                id = localItem?.id ?: 0,
                                checklistId = localChecklistId,
                                content = doc.getString("content") ?: "",
                                isChecked = doc.getBoolean("isChecked") ?: false,
                                order = doc.getLong("order")?.toInt() ?: 0,
                                firebaseId = doc.id,
                                lastModified = remoteLastModified
                            )
                            database.checklistDao().insertItem(item)
                        }
                    }
                }
            }
    }

    fun stopSync() {
        noteListener?.remove()
        checklistListener?.remove()
        passwordListener?.remove()
    }

    fun uploadNote(note: NoteEntity) {
        if (!note.isShared) return
        val data = hashMapOf(
            "title" to note.title,
            "content" to note.content,
            "tags" to note.tags,
            "color" to note.color,
            "timestamp" to note.timestamp,
            "lastModified" to note.lastModified
        )
        firestore.collection("users").document(currentUserId).collection("notes")
            .document(note.firebaseId ?: firestore.collection("notes").document().id)
            .set(data)
    }

    fun uploadChecklist(checklist: ChecklistEntity) {
        if (!checklist.isShared) return
        val data = hashMapOf(
            "title" to checklist.title,
            "timestamp" to checklist.timestamp,
            "lastModified" to checklist.lastModified
        )
        val docId = checklist.firebaseId ?: firestore.collection("checklists").document().id
        firestore.collection("users").document(currentUserId).collection("checklists")
            .document(docId).set(data)
    }

    fun uploadChecklistItem(firebaseChecklistId: String, item: ChecklistItemEntity) {
        val data = hashMapOf(
            "content" to item.content,
            "isChecked" to item.isChecked,
            "order" to item.order,
            "lastModified" to item.lastModified
        )
        firestore.collection("users").document(currentUserId).collection("checklists")
            .document(firebaseChecklistId).collection("items")
            .document(item.firebaseId ?: firestore.collection("items").document().id)
            .set(data)
    }

    fun uploadPassword(password: PasswordEntity) {
        if (!password.isShared) return
        val data = hashMapOf(
            "serviceName" to password.serviceName,
            "username" to password.username,
            "encryptedPassword" to password.encryptedPassword,
            "iv" to password.iv,
            "notes" to password.notes,
            "timestamp" to password.timestamp,
            "lastModified" to password.lastModified,
            "customFields" to password.customFields,
            "logoUrl" to password.logoUrl
        )
        val docRef = firestore.collection("users").document(currentUserId).collection("passwords")
            .document(password.firebaseId ?: firestore.collection("passwords").document().id)
        
        // Save current version to history before updating (Version History)
        docRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                docRef.collection("history").add(doc.data!!)
            }
            docRef.set(data)
        }
    }
}
