package app.volapex.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.volapex.desktop.ui.PainelScreen

fun main() {
    val container = DesktopContainer()
    application {
        Window(
            onCloseRequest = {
                container.close()
                exitApplication()
            },
            title = "Volapex",
            state = rememberWindowState(width = 1100.dp, height = 820.dp),
        ) {
            PainelScreen(container)
        }
    }
}
