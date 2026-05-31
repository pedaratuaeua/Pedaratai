package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AgentsScreen
import com.example.ui.ChatScreen
import com.example.ui.HistoryScreen
import com.example.ui.MainViewModel
import com.example.ui.SettingsScreen
import com.example.ui.Translations
import com.example.ui.theme.PedaratAITheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by viewModel.theme.collectAsState()
            val langState by viewModel.language.collectAsState()

            PedaratAITheme(themeName = themeState) {
                // Configure LayoutDirection for English vs Persian RTL
                val layoutDir = if (langState == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(innerPadding)
                        ) {
                            var activeTab by remember { mutableStateOf("agents") }

                            // Active view loader using fade animations
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (activeTab) {
                                    "chat" -> ChatScreen(viewModel = viewModel, lang = langState)
                                    "agents" -> AgentsScreen(
                                        viewModel = viewModel,
                                        lang = langState,
                                        onNavigateToChat = { activeTab = "chat" }
                                    )
                                    "history" -> HistoryScreen(
                                        viewModel = viewModel,
                                        lang = langState,
                                        onNavigateToChat = { activeTab = "chat" }
                                    )
                                    "settings" -> SettingsScreen(viewModel = viewModel, lang = langState)
                                }
                            }

                            // Premium Glassmorphic Bottom Navigation
                            GlassNavigationBar(
                                activeTab = activeTab,
                                onTabSelected = { activeTab = it },
                                lang = langState,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    lang: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(68.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                clip = false,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            // Semi-transparent overlay creating the premium glassmorphism frost effect
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = Translations.getString("agents_tab", lang),
                iconSelected = Icons.Filled.SmartToy,
                iconUnselected = Icons.Outlined.SmartToy,
                isSelected = activeTab == "agents",
                onClick = { onTabSelected("agents") }
            )

            NavBarItem(
                label = Translations.getString("chat_tab", lang),
                iconSelected = Icons.Filled.ChatBubble,
                iconUnselected = Icons.Outlined.ChatBubbleOutline,
                isSelected = activeTab == "chat",
                onClick = { onTabSelected("chat") }
            )

            NavBarItem(
                label = Translations.getString("history_tab", lang),
                iconSelected = Icons.Filled.History,
                iconUnselected = Icons.Outlined.History,
                isSelected = activeTab == "history",
                onClick = { onTabSelected("history") }
            )

            NavBarItem(
                label = Translations.getString("settings_tab", lang),
                iconSelected = Icons.Filled.Settings,
                iconUnselected = Icons.Outlined.Settings,
                isSelected = activeTab == "settings",
                onClick = { onTabSelected("settings") }
            )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    label: String,
    iconSelected: ImageVector,
    iconUnselected: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tintColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val itemBg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent

    Box(
        modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(itemBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) iconSelected else iconUnselected,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = tintColor,
                maxLines = 1
            )
        }
    }
}
