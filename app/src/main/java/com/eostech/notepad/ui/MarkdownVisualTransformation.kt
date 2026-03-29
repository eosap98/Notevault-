package com.eostech.notepad.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

/**
 * A perfectly stable VisualTransformation that retains all markdown syntax characters
 * (like `**bold**`, `_italic_`) in text length and cursor offset tracking, but applies
 * the corresponding text spans on-the-fly for real-time live preview.
 * This guarantees 0% offset mapping bugs.
 */
class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val markdown = text.text
        val annotatedString = buildAnnotatedString {
            append(markdown)
            
            // Dim down the markdown syntax markers
            val dimColor = Color.Gray.copy(alpha = 0.5f)

            // 1. Bold: **text**
            val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
            boldRegex.findAll(markdown).forEach { match ->
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                // Color the '*' markers
                addStyle(SpanStyle(color = dimColor), match.range.first, match.range.first + 2)
                addStyle(SpanStyle(color = dimColor), match.range.last - 1, match.range.last + 1)
            }

            // 2. Italic: _text_
            val italicRegex = Regex("(?<![a-zA-Z0-9])_(.*?)_(?![a-zA-Z0-9])")
            italicRegex.findAll(markdown).forEach { match ->
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
                addStyle(SpanStyle(color = dimColor), match.range.first, match.range.first + 1)
                addStyle(SpanStyle(color = dimColor), match.range.last, match.range.last + 1)
            }

            // 3. Heading: ### text
            val headingRegex = Regex("(?m)^### (.*)$")
            headingRegex.findAll(markdown).forEach { match ->
                addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp), match.range.first, match.range.last + 1)
                addStyle(SpanStyle(color = dimColor), match.range.first, match.range.first + 4)
            }
            
            // 4. Bullets: - item
            val bulletRegex = Regex("(?m)^- (.*)$")
            bulletRegex.findAll(markdown).forEach { match ->
                addStyle(SpanStyle(color = dimColor), match.range.first, match.range.first + 2)
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}
