package nihongo.tools.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nihongo.tools.model.AppTool
import nihongo.tools.tools.ToolCatalog

private val Win95Background = Color(0xFF008080)
private val Win95DesktopShadow = Color(0xFF005A5A)
private val Win95Window = Color(0xFFC0C0C0)
private val Win95WindowDark = Color(0xFF808080)
private val Win95WindowDarker = Color(0xFF404040)
private val Win95WindowLight = Color(0xFFDFDFDF)
private val Win95WindowBright = Color(0xFFFFFFFF)
private val Win95TitleStart = Color(0xFF0A246A)
private val Win95TitleEnd = Color(0xFF6A90D5)
private val Win95Text = Color(0xFF000000)
private val Win95Muted = Color(0xFF3F3F3F)
private val Win95Selection = Color(0xFF000080)

private val Win95Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun NihongoToolsApp() {
    val colorScheme = lightColorScheme(
        primary = Win95Selection,
        onPrimary = Win95WindowBright,
        secondary = Win95WindowDark,
        onSecondary = Win95WindowBright,
        tertiary = Win95TitleEnd,
        onTertiary = Win95Text,
        background = Win95Background,
        onBackground = Win95Text,
        surface = Win95Window,
        onSurface = Win95Text,
        surfaceVariant = Win95WindowLight,
        onSurfaceVariant = Win95Muted,
        outline = Win95WindowDarker
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Win95Typography,
        shapes = Shapes(
            small = RoundedCornerShape(0.dp),
            medium = RoundedCornerShape(0.dp),
            large = RoundedCornerShape(0.dp)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot() {
    var selectedTool by remember { mutableStateOf<AppTool?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Win95Background,
                        Win95DesktopShadow
                    )
                )
            )
            .padding(18.dp)
    ) {
        DesktopDecor()
        if (selectedTool == null) {
            ToolMenu(
                tools = ToolCatalog.tools,
                onSelect = { selectedTool = it }
            )
        } else {
            selectedTool?.Content(onBack = { selectedTool = null })
        }
    }
}

@Composable
private fun ToolMenu(
    tools: List<AppTool>,
    onSelect: (AppTool) -> Unit
) {
    Win95WindowFrame(
        modifier = Modifier.fillMaxSize(),
        title = "Nihongo Tools",
        titleTrailing = "Menu"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Desktop utilities for kanji counts and audio downloads.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Choose a tool:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tools) { tool ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(tool) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = tool.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tool.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "> Open",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(12.dp)
                )
            }
        }
    }
}

@Composable
fun ToolScaffold(
    title: String,
    description: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Win95WindowFrame(
        modifier = Modifier.fillMaxSize(),
        title = title,
        titleTrailing = "Tool"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Назад")
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .padding(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun Win95WindowFrame(
    modifier: Modifier = Modifier,
    title: String,
    titleTrailing: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(Win95WindowDarker)
            .padding(2.dp)
            .background(Win95WindowBright)
            .padding(start = 2.dp, top = 2.dp, end = 0.dp, bottom = 0.dp)
            .background(Win95WindowDark)
            .padding(0.dp, 0.dp, 2.dp, 2.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Win95TitleStart, Win95TitleEnd)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            if (titleTrailing != null) {
                Text(
                    text = titleTrailing,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun BoxScope.DesktopDecor() {
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
            .size(width = 180.dp, height = 32.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 16.dp)
            .size(14.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(Color(0xFF00A000))
    )
}
