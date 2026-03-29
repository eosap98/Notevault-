package com.eostech.notepad.ui

import androidx.lifecycle.*
import com.eostech.notepad.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(private val dao: NoteDao, private val syncManager: FirebaseSyncManager) : ViewModel() {
    private val _isSharedMode = MutableStateFlow(false)
    val isSharedMode: StateFlow<Boolean> = _isSharedMode

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notes: LiveData<List<NoteEntity>> = _isSharedMode.flatMapLatest { isShared ->
        dao.getNotes(isShared)
    }.asLiveData()

    fun setSharedMode(isShared: Boolean) {
        _isSharedMode.value = isShared
    }

    fun insert(note: NoteEntity) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val finalNote = note.copy(
            isShared = note.isShared,
            lastModified = now,
            timestamp = if (note.id == 0L) now else note.timestamp,
            fontFamily = note.fontFamily,
            fontSize = note.fontSize
        )
        val id = dao.insertNote(finalNote)
        if (finalNote.isShared) {
            syncManager.uploadNote(finalNote.copy(id = id))
        }
    }

    fun delete(note: NoteEntity) = viewModelScope.launch {
        dao.deleteNote(note)
    }
}

class ChecklistViewModel(private val dao: ChecklistDao, private val syncManager: FirebaseSyncManager) : ViewModel() {
    private val _isSharedMode = MutableStateFlow(false)
    val isSharedMode: StateFlow<Boolean> = _isSharedMode

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val checklists: LiveData<List<ChecklistEntity>> = _isSharedMode.flatMapLatest { isShared ->
        dao.getChecklists(isShared)
    }.asLiveData()

    fun setSharedMode(isShared: Boolean) {
        _isSharedMode.value = isShared
    }

    fun getItems(checklistId: Long): LiveData<List<ChecklistItemEntity>> =
        dao.getItemsForChecklist(checklistId).asLiveData()

    fun saveChecklist(id: Long, title: String, isShared: Boolean, fontFamily: String = "Default", fontSize: Float = 16f, onSaved: ((Long) -> Unit)? = null) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val checklist = ChecklistEntity(
            id = id,
            title = title, 
            isShared = isShared, 
            lastModified = now,
            timestamp = if (id == 0L) now else now,
            fontFamily = fontFamily,
            fontSize = fontSize
        )
        val savedId = dao.insertChecklist(checklist)
        onSaved?.invoke(savedId)
        if (checklist.isShared) {
            syncManager.uploadChecklist(checklist.copy(id = savedId))
        }
    }

    fun addItem(checklistId: Long, content: String, parentId: Long? = null) = viewModelScope.launch {
        dao.insertItem(ChecklistItemEntity(
            checklistId = checklistId, 
            content = content, 
            parentItemId = parentId,
            lastModified = System.currentTimeMillis()
        ))
    }

    fun toggleItem(item: ChecklistItemEntity) = viewModelScope.launch {
        dao.updateItem(item.copy(isChecked = !item.isChecked, lastModified = System.currentTimeMillis()))
    }
    
    fun deleteChecklist(checklist: ChecklistEntity) = viewModelScope.launch {
        dao.deleteChecklist(checklist)
    }
}

class PasswordViewModel(private val dao: PasswordDao, private val syncManager: FirebaseSyncManager) : ViewModel() {
    private val _isSharedMode = MutableStateFlow(false)
    val isSharedMode: StateFlow<Boolean> = _isSharedMode

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val passwords: LiveData<List<PasswordEntity>> = _isSharedMode.flatMapLatest { isShared ->
        dao.getPasswords(isShared)
    }.asLiveData()

    fun setSharedMode(isShared: Boolean) {
        _isSharedMode.value = isShared
    }

    fun insert(password: PasswordEntity) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val finalPassword = password.copy(
            isShared = password.isShared,
            lastModified = now,
            timestamp = if (password.id == 0L) now else password.timestamp
        )
        dao.insertPassword(finalPassword)
        if (finalPassword.isShared) {
            syncManager.uploadPassword(finalPassword)
        }
    }

    fun delete(password: PasswordEntity) = viewModelScope.launch {
        dao.deletePassword(password)
    }
}

class ViewModelFactory(private val dao: Any, private val syncManager: FirebaseSyncManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NoteViewModel::class.java) -> NoteViewModel(dao as NoteDao, syncManager) as T
            modelClass.isAssignableFrom(ChecklistViewModel::class.java) -> ChecklistViewModel(dao as ChecklistDao, syncManager) as T
            modelClass.isAssignableFrom(PasswordViewModel::class.java) -> PasswordViewModel(dao as PasswordDao, syncManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
