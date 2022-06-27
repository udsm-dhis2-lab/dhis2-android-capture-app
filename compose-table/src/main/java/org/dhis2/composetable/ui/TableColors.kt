package org.dhis2.composetable.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TableColors(
    val primary: Color = Color(0xFF2C98F0),
    val primaryLight: Color = Color(0x332C98F0),
    val headerText: Color = Color(0x8A000000),
    val headerBackground1: Color = Color(0x05000000),
    val headerBackground2: Color = Color(0x0A000000),
    val cellText: Color = Color(0xDE000000),
    val disabledCellText: Color = Color(0x61000000),
    val disabledCellBackground: Color = Color(0x0A000000),
    val errorColor: Color = Color(0xFFE91E63),
    val tableBackground: Color = Color(0xFFFFFFFF)
)

val LocalTableColors = staticCompositionLocalOf { TableColors() }

@Composable
fun TableTheme(
    tableColors: TableColors?,
    content:
        @Composable
        () -> Unit
) {
    CompositionLocalProvider(
        LocalTableColors provides (tableColors ?: TableColors())
    ) {
        MaterialTheme(
            content = content
        )
    }
}

object TableTheme {
    val colors: TableColors
        @Composable
        get() = LocalTableColors.current
}
