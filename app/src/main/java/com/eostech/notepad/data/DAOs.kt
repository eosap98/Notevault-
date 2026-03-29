package com.eostech.notepad.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isShared = :isShared ORDER BY timestamp DESC")
    fun getNotes(isShared: Boolean): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getNoteByFirebaseId(firebaseId: String): NoteEntity?
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklists WHERE isShared = :isShared ORDER BY timestamp DESC")
    fun getChecklists(isShared: Boolean): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY `order` ASC")
    fun getItemsForChecklist(checklistId: Long): Flow<List<ChecklistItemEntity>>

    @Upsert
    suspend fun insertChecklist(checklist: ChecklistEntity): Long

    @Upsert
    suspend fun insertItem(item: ChecklistItemEntity)

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Delete
    suspend fun deleteChecklist(checklist: ChecklistEntity)

    @Query("SELECT * FROM checklists WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getChecklistByFirebaseId(firebaseId: String): ChecklistEntity?

    @Query("SELECT * FROM checklist_items WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getItemByFirebaseId(firebaseId: String): ChecklistItemEntity?
}

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords WHERE isShared = :isShared ORDER BY timestamp DESC")
    fun getPasswords(isShared: Boolean): Flow<List<PasswordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordEntity)

    @Delete
    suspend fun deletePassword(password: PasswordEntity)

    @Query("SELECT * FROM passwords WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getPasswordByFirebaseId(firebaseId: String): PasswordEntity?
}
