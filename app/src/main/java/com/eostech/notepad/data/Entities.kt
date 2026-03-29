package com.eostech.notepad.data

import androidx.room.*

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String, // Comma-separated tags
    val color: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isShared: Boolean = false,
    val firebaseId: String? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val fontFamily: String = "Default",
    val fontSize: Float = 16f
)

@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isShared: Boolean = false,
    val firebaseId: String? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val fontFamily: String = "Default",
    val fontSize: Float = 16f
)

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistEntity::class,
            parentColumns = ["id"],
            childColumns = ["checklistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("checklistId")]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checklistId: Long,
    val content: String,
    val isChecked: Boolean = false,
    val parentItemId: Long? = null, // For nested items
    val order: Int = 0,
    val firebaseId: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String,
    val username: String,
    val encryptedPassword: String,
    val iv: String, // Initialization vector for encryption
    val category: String = "General",
    val notes: String = "",
    val customFields: String = "[]", // JSON array of {label: string, value: string}
    val logoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isShared: Boolean = false,
    val firebaseId: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)
