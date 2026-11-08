package app.virtualwheelracing.desktop

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.virtualwheelracing.desktop.ui.MainScreen

fun main() {
    val container = DesktopContainer()
    val icon = DesktopContainer::class.java.getResourceAsStream("/icon.png")?.use { BitmapPainter(loadImageBitmap(it)) }
    application {
        Window(
            onCloseRequest = {
                container.close()
                exitApplication()
            },
            title = "Virtual Wheel Racing",
            icon = icon,
            state = rememberWindowState(width = 1100.dp, height = 820.dp),
        ) {
            MainScreen(container)
        }
    }
}
