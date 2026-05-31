package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.SavedChatMessage
import com.example.data.SavedChatSession
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val messages by viewModel.messages.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedAgent by viewModel.selectedAgent.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp) // Leave space for floating navigation bar
    ) {
        // Chat Header with Premium Design template theme
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Gradient-br box with first letter/icon of agent
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFA8C7FA), Color(0xFF7CACF8))
                            )
                        )
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedAgent?.icon?.take(1) ?: "P",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF062E6F)
                    )
                }

                Column {
                    Text(
                        text = activeSession?.title ?: selectedAgent?.let { if (lang == "fa") it.faName else it.name } ?: "Pedarat AI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = selectedAgent?.let { if (lang == "fa") it.faDescription else it.description } ?: "LLM7 ENGINE",
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // New session button
                IconButton(
                    onClick = {
                        selectedAgent?.let { viewModel.createNewSession(it) }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Session",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Export session button
                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.exportChatHistory(context)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export History",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.08f),
            thickness = 1.dp
        )

        // Empty state
        if (messages.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Elevated beautiful avatar with dual circle layers
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedAgent?.icon ?: "👵",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (lang == "fa") {
                            "چطور می‌تونم کمکت کنم؟ پیامت رو با من به اشتراک بذار."
                        } else {
                            "How can I assist you today? Send a message to start."
                        },
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (lang == "fa") "عامل متصل: ${selectedAgent?.faName}" else "Active Agent: ${selectedAgent?.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message, lang = lang)
                }
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (lang == "fa") "در حال دریافت پاسخ..." else "Thinking...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Floating input area styled EXACTLY like the premium dark capsule input in the design HTML
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(
                            text = if (selectedAgent?.mode == "image") {
                                Translations.getString("prompt_placeholder", lang)
                            } else {
                                Translations.getString("chat_hint", lang)
                            },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputMessage.isNotBlank()) {
                                viewModel.sendMessage(inputMessage)
                                inputMessage = ""
                            }
                        }
                    )
                )

                // Render Action Button matching A8C7FA palette of HTML spec
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputMessage.isNotBlank()) {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFA8C7FA), Color(0xFF7CACF8))
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f))
                                )
                            }
                        )
                        .clickable(enabled = inputMessage.isNotBlank()) {
                            viewModel.sendMessage(inputMessage)
                            inputMessage = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedAgent?.mode == "image") Icons.Default.Image else Icons.Default.Send,
                        contentDescription = "Send/Generate",
                        tint = if (inputMessage.isNotBlank()) Color(0xFF062E6F) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: SavedChatMessage,
    lang: String
) {
    val isUser = message.role == "user"
    val isError = message.role == "error"
    var showImageDialog by remember { mutableStateOf(false) }

    // Check if background dark shades are used to apply precise high-contrast frosted rules
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0F1115) || MaterialTheme.colorScheme.background == Color(0xFF0E0B16)

    val bubbleBg = when {
        isUser -> if (isDark) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
        isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.65f) // Glassy semi-transparent AI bubble
    }

    val bubbleBorder = if (!isUser && !isError) {
        Modifier.border(
            BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
                )
            ),
            RoundedCornerShape(16.dp)
        )
    } else Modifier

    val textCol = when {
        isUser -> if (isDark) Color(0xFF00315C) else MaterialTheme.colorScheme.onPrimary
        isError -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val bubbleAlignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = bubbleAlignment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(bubbleBg)
                    .then(bubbleBorder)
                    .padding(14.dp)
            ) {
                // Frosted design AI custom indicator matching template HTML exactly!
                if (!isUser && !isError) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF062E6F)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == "fa") "پدرت هوش مصنوعی" else "PEDARAT LLM7",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Image message render
                if (message.imageUrl != null) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Generated art",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showImageDialog = true }
                            .padding(bottom = 8.dp)
                    )
                }

                // Text body with full Markdown rendering
                if (isUser) {
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        color = textCol,
                        lineHeight = 22.sp
                    )
                } else {
                    MarkdownText(
                        text = message.content,
                        textColor = textCol,
                        currentLang = lang
                    )
                }
            }
        }
    }

    // Modal view for enlarged image
    if (showImageDialog && message.imageUrl != null) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Enlarged photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showImageDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = Translations.getString("back", lang),
                            color = Color(0xFF062E6F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgentsScreen(
    viewModel: MainViewModel,
    lang: String,
    onNavigateToChat: () -> Unit
) {
    val selectedAgent by viewModel.selectedAgent.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App introduction banner card styled beautifully in high contrast glassmorphism
        item {
            val shape = RoundedCornerShape(24.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
                            )
                        ),
                        shape
                    ),
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        // Icon matching primary sky-blue design
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFA8C7FA), Color(0xFF7CACF8))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF062E6F)
                            )
                        }
                        Column {
                            Text(
                                text = Translations.getString("app_title", lang),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "LLM7 ENGINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "fa") {
                            "به دنیای هوش مصنوعی پدرت خوش آمدید! کدهای برنامه‌نویسی را تولید، مقالات متنی را ویرایش کرده یا تصاویر خلاق هنری را به صورت کاملاً رایگان تجربه کنید."
                        } else {
                            "Welcome to Pedarat AI! Generate expert code, chat with a clever Iranian father, or create majestic AI-generated art with our free visual engines."
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                    )
                }
            }
        }

        // Text Agents Section
        item {
            Text(
                text = Translations.getString("text_agents", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        items(viewModel.textAgents) { agent ->
            AgentCard(
                agent = agent,
                isSelected = selectedAgent == agent,
                lang = lang,
                onClick = {
                    viewModel.selectAgent(agent)
                    onNavigateToChat()
                }
            )
        }

        // Image Agents Section
        item {
            Text(
                text = Translations.getString("image_agents", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )
        }

        items(viewModel.imageAgents) { agent ->
            AgentCard(
                agent = agent,
                isSelected = selectedAgent == agent,
                lang = lang,
                onClick = {
                    viewModel.selectAgent(agent)
                    onNavigateToChat()
                }
            )
        }
    }
}

@Composable
fun AgentCard(
    agent: Agent,
    isSelected: Boolean,
    lang: String,
    onClick: () -> Unit
) {
    val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f)
    val cardBg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = borderCol
                ),
                shape = shape
            ),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = agent.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (lang == "fa") agent.faName else agent.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (lang == "fa") agent.faDescription else agent.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    lang: String,
    onNavigateToChat: () -> Unit
) {
    val savedSessions by viewModel.savedSessions.collectAsState()

    if (savedSessions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Translations.getString("no_sessions", lang),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(savedSessions) { session ->
                HistoryItemCard(
                    session = session,
                    lang = lang,
                    onClick = {
                        viewModel.loadSession(session)
                        onNavigateToChat()
                    },
                    onDelete = {
                        viewModel.deleteSession(session.id)
                    }
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    session: SavedChatSession,
    lang: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f))
                    )
                ),
                shape
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (session.agentMode == "image") "📸" else "💬",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = session.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = session.agentName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Session",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val themeChoice by viewModel.theme.collectAsState()

    var keyInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(apiKey, baseUrl) {
        keyInput = apiKey ?: ""
        urlInput = baseUrl
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App logo & name elegant header icon with dual circle layers
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 38.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Translations.getString("app_title", lang),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "v1.0.0",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Saved API Configuration
        item {
            val shape = RoundedCornerShape(20.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f))
                            )
                        ),
                        shape
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                ),
                shape = shape
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "API Key & Endpoint Configuration",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text(Translations.getString("api_key_label", lang)) },
                        placeholder = { Text(Translations.getString("api_key_placeholder", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text(Translations.getString("base_url_label", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.saveApiKeySettings(keyInput, urlInput)
                            Toast.makeText(context, Translations.getString("save_success", lang), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lang == "fa") "ذخیره کردن تنظیمات" else "Save Configurations",
                            color = Color(0xFF062E6F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Language toggle
        item {
            val shape = RoundedCornerShape(20.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f))
                            )
                        ),
                        shape
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                ),
                shape = shape
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = Translations.getString("language_label", lang),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveLanguage("en") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "English",
                                color = if (lang == "en") Color(0xFF062E6F) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.saveLanguage("fa") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "fa") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "فارسی",
                                color = if (lang == "fa") Color(0xFF062E6F) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Visual Themes selection
        item {
            val shape = RoundedCornerShape(20.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f))
                            )
                        ),
                        shape
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                ),
                shape = shape
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = Translations.getString("theme_label", lang),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThemeSelectionRow(
                            name = Translations.getString("theme_dark", lang),
                            isSelected = themeChoice == "dark",
                            colorsList = listOf(Color(0xFF0F1115), Color(0xFFA8C7FA)),
                            onClick = { viewModel.saveTheme("dark") }
                        )

                        ThemeSelectionRow(
                            name = Translations.getString("theme_white", lang),
                            isSelected = themeChoice == "white",
                            colorsList = listOf(Color(0xFFF3F4F6), Color(0xFF2563EB)),
                            onClick = { viewModel.saveTheme("white") }
                        )

                        ThemeSelectionRow(
                            name = Translations.getString("theme_colorful", lang),
                            isSelected = themeChoice == "colorful",
                            colorsList = listOf(Color(0xFF0E0B16), Color(0xFFFF3366)),
                            onClick = { viewModel.saveTheme("colorful") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionRow(
    name: String,
    isSelected: Boolean,
    colorsList: List<Color>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Colors preview dots
            colorsList.forEach { col ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(col)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}
