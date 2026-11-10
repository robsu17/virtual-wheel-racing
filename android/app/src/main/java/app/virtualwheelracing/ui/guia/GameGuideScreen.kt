package app.virtualwheelracing.ui.guia

import androidx.annotation.StringRes
import app.virtualwheelracing.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.virtualwheelracing.ui.components.ScreenHeader
import app.virtualwheelracing.ui.components.VwrCard
import app.virtualwheelracing.ui.theme.Fonts
import app.virtualwheelracing.ui.theme.Palette

/** Passo a passo curto por jogo. Os nomes de menu variam com a versão; por isso o texto é genérico. */
enum class Game(@StringRes val label: Int, val steps: List<Int>, @StringRes val tip: Int?) {
    ETS2(R.string.game_ets2, listOf(R.string.guide_ets2_1, R.string.guide_ets2_2, R.string.guide_ets2_3), R.string.guide_ets2_tip),
    ASSETTO(R.string.game_assetto, listOf(R.string.guide_assetto_1, R.string.guide_assetto_2, R.string.guide_assetto_3), null),
    BEAMNG(R.string.game_beamng, listOf(R.string.guide_beamng_1, R.string.guide_beamng_2, R.string.guide_beamng_3), null),
    RFACTOR(R.string.game_rfactor, listOf(R.string.guide_rfactor_1, R.string.guide_rfactor_2, R.string.guide_rfactor_3), R.string.guide_rfactor_tip),
    OTHER(R.string.game_other, listOf(R.string.guide_other_1, R.string.guide_other_2, R.string.guide_other_3), R.string.guide_other_tip),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameGuideScreen(onSetupMode: () -> Unit, onBack: () -> Unit) {
    var game by rememberSaveable { mutableStateOf(Game.ETS2) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(stringResource(R.string.setup_my_game), stringResource(R.string.guide_subtitle), onBack)

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Game.entries.forEach { option ->
                val active = option == game
                Text(
                    stringResource(option.label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) Palette.OnAccent else Palette.Text,
                    modifier = Modifier
                        .background(if (active) Palette.Accent else Palette.Surface2, RoundedCornerShape(999.dp))
                        .clickable(role = Role.Tab) { game = option }
                        .semantics { selected = active }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }

        VwrCard {
            game.steps.forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(28.dp).background(Palette.Accent, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Palette.OnAccent, fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(stringResource(step), fontSize = 15.sp, lineHeight = 21.sp, color = Palette.Text, modifier = Modifier.weight(1f))
                }
            }
            game.tip?.let { Text(stringResource(it), fontSize = 13.sp, color = Palette.Muted) }
        }

        VwrCard {
            Text(stringResource(R.string.setup_mode), fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Palette.Text)
            Text(
                stringResource(R.string.setup_mode_body),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Palette.Muted,
            )
            Button(onClick = onSetupMode, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.open_setup_mode), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        VwrCard {
            Text(stringResource(R.string.every_game), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            listOf(
                stringResource(R.string.every_game_deadzone),
                stringResource(R.string.every_game_ffb),
            ).forEach { Text("•  $it", fontSize = 14.sp, lineHeight = 20.sp, color = Palette.Muted) }
        }
    }
}
