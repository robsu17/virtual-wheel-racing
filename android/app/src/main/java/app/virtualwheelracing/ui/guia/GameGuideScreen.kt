package app.virtualwheelracing.ui.guia

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
enum class Game(val label: String, val steps: List<String>, val tip: String?) {
    ETS2(
        "Euro Truck",
        listOf(
            "Em Opções > Controles, escolha o tipo \"Volante e pedais\" ou \"Teclado + Joystick\".",
            "Selecione o vJoy Device como controle.",
            "Em Direção, Acelerador e Freio, clique no campo e use o modo configuração abaixo.",
        ),
        "Vale o mesmo para o American Truck Simulator.",
    ),
    ASSETTO(
        "Assetto Corsa",
        listOf(
            "No Content Manager ou em Opções > Controles, escolha \"Volante\".",
            "Selecione o vJoy Device.",
            "Clique em Direção, Acelerador e Freio e use o modo configuração abaixo.",
        ),
        null,
    ),
    BEAMNG(
        "BeamNG",
        listOf(
            "Em Opções > Controles, encontre o vJoy Device na lista.",
            "Associe Direção, Acelerador e Freio usando o modo configuração abaixo.",
            "Em Filtros de direção, escolha \"Direto\" para sentir como um volante.",
        ),
        null,
    ),
    RFACTOR(
        "rFactor",
        listOf(
            "Em Controles, escolha vJoy Device como controlador.",
            "Clique em Direção, Acelerador e Freio e use o modo configuração abaixo.",
            "Não é preciso inverter nenhum eixo.",
        ),
        "Testado e aprovado no rFactor 1.",
    ),
    OTHER(
        "Outro",
        listOf(
            "Procure a tela de Controles do jogo e escolha vJoy Device.",
            "Para cada comando (Direção, Acelerador, Freio), clique no campo e use o modo configuração abaixo.",
            "Se um pedal ficar ao contrário, use a opção de inverter eixo do jogo.",
        ),
        "Jogos que só aceitam controle de Xbox podem não reconhecer.",
    ),
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
        ScreenHeader("Configurar meu jogo", "Só na primeira vez em cada jogo", onBack)

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Game.entries.forEach { option ->
                val active = option == game
                Text(
                    option.label,
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
                    Text(step, fontSize = 15.sp, lineHeight = 21.sp, color = Palette.Text, modifier = Modifier.weight(1f))
                }
            }
            game.tip?.let { Text(it, fontSize = 13.sp, color = Palette.Muted) }
        }

        VwrCard {
            Text("Modo configuração", fontFamily = Fonts.Display, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Palette.Text)
            Text(
                "Quando o jogo pedir um comando, toque nele no celular (Direção, Acelerador ou Freio) e mexa só aquilo. Assim o jogo não confunde um com o outro.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Palette.Muted,
            )
            Button(onClick = onSetupMode, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Abrir modo configuração", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        VwrCard {
            Text("Em todo jogo", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Text)
            listOf(
                "Zona morta do jogo em 0%: o app já cuida disso.",
                "Desligue o force feedback: o celular não empurra de volta.",
            ).forEach { Text("•  $it", fontSize = 14.sp, lineHeight = 20.sp, color = Palette.Muted) }
        }
    }
}
