package com.eostech.notepad.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupData(
    val notes: List<NoteEntity>,
    val checklists: List<ChecklistEntity>,
    val checklistItems: List<ChecklistItemEntity>,
    val passwords: List<PasswordEntity>,
    val backupTimestamp: Long = System.currentTimeMillis()
)

class AppBackupManager(
    private val context: Context,
    private val noteDao: NoteDao,
    private val checklistDao: ChecklistDao,
    private val passwordDao: PasswordDao
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val notes = noteDao.getNotes(false).first() + noteDao.getNotes(true).first()
            val checklists = checklistDao.getChecklists(false).first() + checklistDao.getChecklists(true).first()
            val checklistItems = checklists.flatMap { checklistDao.getItemsForChecklist(it.id).first() }
            val passwords = passwordDao.getPasswords(false).first() + passwordDao.getPasswords(true).first()

            val backupData = BackupData(
                notes = notes,
                checklists = checklists,
                checklistItems = checklistItems,
                passwords = passwords
            )

            val json = gson.toJson(backupData)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val backupData = gson.fromJson(reader, BackupData::class.java)
                    
                    // Import Notes (using replace on conflict)
                    backupData.notes.forEach { noteDao.insertNote(it) }
                    
                    // Import Checklists and their Items
                    // Note: We might need to handle ID mapping if we want to avoid replacing existing data with same IDs
                    // but usually a 'Restore' replaces local state or merges.
                    // Given @Upsert and OnConflictStrategy.REPLACE, we'll just insert.
                    
                    backupData.checklists.forEach { checklistDao.insertChecklist(it) }
                    backupData.checklistItems.forEach { checklistDao.insertItem(it) }
                    
                    // Import Passwords
                    backupData.passwords.forEach { passwordDao.insertPassword(it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
