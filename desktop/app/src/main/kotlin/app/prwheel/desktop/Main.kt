package app.prwheel.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.prwheel.desktop.ui.PainelScreen

fun main() {
    val container = DesktopContainer()
    application {
        Window(
            onCloseRequest = {
                container.close()
                exitApplication()
            },
            title = "PR Wheel",
            state = rememberWindowState(width = 1100.dp, height = 820.dp),
        ) {
            PainelScreen(container)
        }
    }
}
