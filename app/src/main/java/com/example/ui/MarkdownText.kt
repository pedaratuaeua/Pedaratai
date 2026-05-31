package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class MarkdownToken {
    data class Header(val level: Int, val text: String) : MarkdownToken()
    data class CodeBlock(val language: String, val code: String) : MarkdownToken()
    data class BulletPoint(val text: String) : MarkdownToken()
    data class Paragraph(val text: String) : MarkdownToken()
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    currentLang: String = "en"
) {
    val tokens = parseMarkdown(text)
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tokens.forEach { token ->
            when (token) {
                is MarkdownToken.Header -> {
                    val size = when (token.level) {
                        1 -> 24.sp
                        2 -> 20.sp
                        else -> 18.sp
                    }
                    Text(
                        text = token.text,
                        fontSize = size,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                is MarkdownToken.BulletPoint -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = renderInlineStyles(token.text),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = textColor
                        )
                    }
                }
                is MarkdownToken.CodeBlock -> {
                    CodeBlockView(language = token.language, code = token.code, context = context, currentLang = currentLang)
                }
                is MarkdownToken.Paragraph -> {
                    Text(
                        text = renderInlineStyles(token.text),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(language: String, code: String, context: Context, currentLang: String) {
    val displayLanguage = if (language.isBlank()) "code" else language

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF000000))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayLanguage.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7CACF8),
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Code", code)
                    clipboard.setPrimaryClip(clip)
                    val toastMsg = Translations.getString("copied", currentLang)
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFFC4C6D0),
                lineHeight = 18.sp
            )
        }
    }
}

fun parseMarkdown(text: String): List<MarkdownToken> {
    val tokens = mutableListOf<MarkdownToken>()
    val lines = text.lines()
    var inCodeBlock = false
    var codeLanguage = ""
    val codeContent = StringBuilder()

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                tokens.add(MarkdownToken.CodeBlock(codeLanguage, codeContent.toString().trimEnd()))
                codeContent.clear()
                inCodeBlock = false
            } else {
                codeLanguage = line.trim().substringAfter("```").trim()
                inCodeBlock = true
            }
        } else if (inCodeBlock) {
            codeContent.append(line).append("\n")
        } else {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val headerText = line.drop(level).trim()
                    tokens.add(MarkdownToken.Header(level, headerText))
                }
                trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    val bulletText = line.trim().drop(1).trim()
                    tokens.add(MarkdownToken.BulletPoint(bulletText))
                }
                trimmed.isNotBlank() -> {
                    tokens.add(MarkdownToken.Paragraph(line))
                }
            }
        }
    }
    if (inCodeBlock && codeContent.isNotEmpty()) {
        tokens.add(MarkdownToken.CodeBlock(codeLanguage, codeContent.toString().trimEnd()))
    }
    return tokens
}

fun renderInlineStyles(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val nextBoldStart = text.indexOf("**", cursor)
            if (nextBoldStart == -1) {
                append(text.substring(cursor))
                break
            }
            append(text.substring(cursor, nextBoldStart))
            val nextBoldEnd = text.indexOf("**", nextBoldStart + 2)
            if (nextBoldEnd == -1) {
                append("**")
                cursor = nextBoldStart + 2
                continue
            }
            val boldContent = text.substring(nextBoldStart + 2, nextBoldEnd)
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(boldContent)
            pop()
            cursor = nextBoldEnd + 2
        }
    }
}
