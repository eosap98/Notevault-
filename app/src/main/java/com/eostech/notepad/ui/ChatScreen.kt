package com.eostech.notepad.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eostech.notepad.data.FirebaseSyncManager
import com.eostech.notepad.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "user_placeholder",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, sync: FirebaseSyncManager, vaultId: String?, myId: String) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationHelper = remember { com.eostech.notepad.util.NotificationHelper(context) }
    // No redundant collabManager needed here for myId
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(true) }

    // Load messages from Firestore realtime
    LaunchedEffect(vaultId) {
        if (vaultId == null) return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()
        db.collection("vaults").document(vaultId)
            .collection("chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                isLoading = false
                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        val msg = ChatMessage(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        messages.add(msg)
                        
                        // Show notification for new message from partner
                        // Check if it's very fresh (last 2 seconds) to avoid spamming on initial load
                        val isFresh = System.currentTimeMillis() - msg.timestamp < 2000
                        if (msg.senderId != myId && isFresh) {
                            notificationHelper.showNotification("Pesan Baru", msg.text)
                        }
                    }
                    scope.launch {
                        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
    }

    fun sendMessage() {
        val text = messageText.trim()
        if (text.isEmpty() || vaultId == null) return
        messageText = ""
        val msg = ChatMessage(senderId = myId, text = text)
        scope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("vaults").document(vaultId)
                    .collection("chat")
                    .document(msg.id)
                    .set(mapOf(
                        "senderId" to msg.senderId,
                        "text" to msg.text,
                        "timestamp" to msg.timestamp
                    )).await()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat".tr(), color = CharcoalTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CharcoalTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp, color = SurfaceWhite) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Ketik pesan...".tr(), color = CharcoalBody.copy(alpha = 0.4f)) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = CharcoalTitle,
                            unfocusedTextColor = CharcoalTitle
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    if (vaultId == null) {
                        Text("Tidak ada vault".tr(), style = MaterialTheme.typography.labelSmall, color = CharcoalBody)
                    } else {
                        IconButton(
                            onClick = { sendMessage() },
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Kirim".tr(),
                                tint = if (messageText.isNotBlank()) SoftBlue else SoftGrey
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (vaultId == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LinkOff, contentDescription = null, tint = SoftGrey, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Belum terhubung dengan partner.".tr(), color = CharcoalBody)
                }
            }
        } else if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SoftBlue)
            }
        } else if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = SoftGrey, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Belum ada pesan. Ucapkan halo!".tr(), color = CharcoalBody)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == myId
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 2.dp,
                                bottomEnd = if (isMe) 2.dp else 16.dp
                            ),
                            color = if (isMe) SoftBlue else SurfaceWhite,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    msg.text,
                                    color = if (isMe) CharcoalTitle else CharcoalBody,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMe) CharcoalTitle.copy(alpha = 0.5f) else CharcoalBody.copy(alpha = 0.4f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
