package app.prwheel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.prwheel.ui.conectar.ConectarScreen
import app.prwheel.ui.dirigir.DirigirScreen
import app.prwheel.ui.theme.PrWheelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as PrWheelApplication).container
        setContent {
            PrWheelTheme { AppRoot(container) }
        }
    }
}

@Composable
private fun AppRoot(container: AppContainer) {
    var driving by rememberSaveable { mutableStateOf(false) }
    var connection by remember { mutableStateOf(container.preferences.load()) }

    if (driving && connection.isValid) {
        DirigirScreen(container, connection, onBack = { driving = false })
    } else {
        ConectarScreen(
            initial = connection,
            onConnect = {
                container.preferences.save(it)
                connection = it
                driving = true
            },
        )
    }
}
