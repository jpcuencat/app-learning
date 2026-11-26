package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SessionMode

enum class NycSceneTheme {
    CAFE,
    STREET,
    HOTEL,
    RESTAURANT,
    OFFICE_AIRPORT
}

data class NycPropItem(
    val phraseEn: String,
    val detailEs: String,
    val priceOrTag: String
)

data class NycWorldPropSpec(
    val theme: NycSceneTheme,
    val buttonLabelEs: String,
    val icon: ImageVector,
    val headerTitle: String,
    val headerSubtitle: String,
    val subwayLines: List<Pair<String, Color>>,
    val weatherAndTime: String,
    val arrivalStationEs: String,
    val items: List<NycPropItem>
)

fun resolveNycWorldSpec(lessonId: String, mode: SessionMode, locationLabel: String, timeLabel: String): NycWorldPropSpec {
    return when (lessonId) {
        "cafe_order", "boss_a1_twist" -> NycWorldPropSpec(
            theme = NycSceneTheme.CAFE,
            buttonLabelEs = "Ver pizarra del café",
            icon = Icons.Filled.LocalCafe,
            headerTitle = if (mode == SessionMode.REVIEW) "WEST VILLAGE TEA BAR" else "SOHO ESPRESSO BAR · NYC",
            headerSubtitle = "Toca una frase del menú para escucharla o usarla en tu pedido",
            subwayLines = listOf("C" to Color(0xFF2850AD), "E" to Color(0xFF2850AD), "6" to Color(0xFF00933C)),
            weatherAndTime = "$timeLabel · 14°C · Despejado",
            arrivalStationEs = "Saliendo de Spring St Station · SoHo",
            items = listOf(
                NycPropItem("Could I have a coffee, please?", "Pedir café con cortesía", "$4.25"),
                NycPropItem("Could I have a tea, please?", "Pedir té en el local", "$4.50"),
                NycPropItem("To go, please", "Para llevar por Nueva York", "TO GO"),
                NycPropItem("For here, please", "Para tomar en la mesa", "HERE"),
                NycPropItem("How much is it?", "Preguntar el precio", "TAX +8.8%"),
                NycPropItem("What is the total?", "Preguntar el total final", "TOTAL")
            )
        )

        "ask_directions" -> NycWorldPropSpec(
            theme = NycSceneTheme.STREET,
            buttonLabelEs = "Ver mapa de la calle",
            icon = Icons.Filled.DirectionsSubway,
            headerTitle = if (mode == SessionMode.REVIEW) "MTA · LEXINGTON & 51ST" else "MANHATTAN · 5TH AVE & 42ND",
            headerSubtitle = "Señalización real de esquina neoyorquina · toca para escuchar",
            subwayLines = listOf("4" to Color(0xFF00933C), "5" to Color(0xFF00933C), "6" to Color(0xFF00933C), "7" to Color(0xFFB933AD)),
            weatherAndTime = "$timeLabel · 16°C · Brisa urbana",
            arrivalStationEs = "Caminando por 5th Ave & 42nd St",
            items = listOf(
                NycPropItem("Excuse me, where is the pharmacy?", "Preguntar por la farmacia", "2 BLOCKS"),
                NycPropItem("Where is the subway station?", "Buscar el metro MTA", "ENTRANCE"),
                NycPropItem("Could you repeat that, please?", "Pedir que repitan más claro", "REPEAT"),
                NycPropItem("So I turn left at the corner?", "Confirmar giro a la izquierda", "← LEFT"),
                NycPropItem("Go straight two blocks", "Seguir recto dos cuadras", "↑ STRAIGHT")
            )
        )

        "hotel_checkin" -> NycWorldPropSpec(
            theme = NycSceneTheme.HOTEL,
            buttonLabelEs = "Ver tarjeta del hotel",
            icon = Icons.Filled.Key,
            headerTitle = "MIDTOWN BOUTIQUE HOTEL · NY",
            headerSubtitle = "Tu pase de check-in en Manhattan · toca para escuchar",
            subwayLines = listOf("N" to Color(0xFFFCCC0A), "Q" to Color(0xFFFCCC0A), "R" to Color(0xFFFCCC0A)),
            weatherAndTime = "$timeLabel · 12°C · Midtown",
            arrivalStationEs = "Entrando al lobby en Midtown Manhattan",
            items = listOf(
                NycPropItem("I have a reservation", "Confirmar tu reserva", "#NY-408"),
                NycPropItem("What is the Wi-Fi password?", "Pedir clave del Wi-Fi", "WI-FI"),
                NycPropItem("What time is breakfast?", "Preguntar horario del desayuno", "7–10 AM"),
                NycPropItem("Could I get a late check-out?", "Pedir salida más tarde", "12:00 PM")
            )
        )

        "restaurant_a2", "boss_a2_twist" -> NycWorldPropSpec(
            theme = NycSceneTheme.RESTAURANT,
            buttonLabelEs = "Ver carta de Brooklyn",
            icon = Icons.Filled.RestaurantMenu,
            headerTitle = locationLabel.uppercase(),
            headerSubtitle = "Carta y frases útiles del bistró neoyorquino",
            subwayLines = listOf("L" to Color(0xFFA7A9AC), "G" to Color(0xFF6CBE45), "F" to Color(0xFFFF6319)),
            weatherAndTime = "$timeLabel · 18°C · Atardecer",
            arrivalStationEs = "Llegando a Williamsburg · Brooklyn",
            items = listOf(
                NycPropItem("A table for two, please", "Pedir mesa para dos", "DINING"),
                NycPropItem("What do you recommend?", "Pedir recomendación local", "CHEF"),
                NycPropItem("Could we have the check, please?", "Pedir la cuenta al final", "CHECK"),
                NycPropItem("Do you have a vegetarian option?", "Preguntar opción vegetariana", "VEGGIE")
            )
        )

        else -> NycWorldPropSpec(
            theme = NycSceneTheme.OFFICE_AIRPORT,
            buttonLabelEs = "Ver pase y agenda",
            icon = Icons.Filled.ConfirmationNumber,
            headerTitle = locationLabel.uppercase(),
            headerSubtitle = "Tarjeta ejecutiva / JFK Terminal 4 · toca para practicar",
            subwayLines = listOf("A" to Color(0xFF2850AD), "E" to Color(0xFF2850AD), "B" to Color(0xFFFF6319)),
            weatherAndTime = "$timeLabel · 15°C · NYC",
            arrivalStationEs = "Conectando en $locationLabel",
            items = listOf(
                NycPropItem("Could we reschedule our meeting?", "Reprogramar reunión con tacto", "CALENDAR"),
                NycPropItem("Does Thursday afternoon work?", "Proponer nuevo horario", "THU 3PM"),
                NycPropItem("My flight was delayed", "Explicar retraso en JFK", "GATE B24"),
                NycPropItem("Thank you for your flexibility", "Agradecer profesionalmente", "PRO")
            )
        )
    }
}

/**
 * Interactive authentic in-world NYC prop modal (SoHo Coffee Chalkboard, MTA Subway Street Sign,
 * Midtown Hotel Keycard, Brooklyn Bistro Menu, or JFK Boarding Pass).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NycWorldPropOverlay(
    spec: NycWorldPropSpec,
    onDismiss: () -> Unit,
    onSpeakPhrase: (String) -> Unit,
    onInsertPhrase: (String) -> Unit
) {
    val boardGradient = when (spec.theme) {
        NycSceneTheme.CAFE -> listOf(Color(0xFF1A2220), Color(0xFF101615)) // Chalkboard slate
        NycSceneTheme.STREET -> listOf(Color(0xFF111418), Color(0xFF0A0D11)) // MTA Black tile
        NycSceneTheme.HOTEL -> listOf(Color(0xFF1C1914), Color(0xFF12100C)) // Brass & obsidian
        NycSceneTheme.RESTAURANT -> listOf(Color(0xFF211714), Color(0xFF140D0B)) // Warm bistro
        NycSceneTheme.OFFICE_AIRPORT -> listOf(Color(0xFF101B29), Color(0xFF0A111C)) // JFK / Wall St Navy
    }
    val accent = when (spec.theme) {
        NycSceneTheme.CAFE -> Color(0xFF63E6BE)
        NycSceneTheme.STREET -> Color(0xFFFCCC0A)
        NycSceneTheme.HOTEL -> Color(0xFFE6C068)
        NycSceneTheme.RESTAURANT -> Color(0xFFFF9F68)
        NycSceneTheme.OFFICE_AIRPORT -> Color(0xFF66D9FF)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB8050910))
            .clickable { onDismiss() }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.55f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .testTag("nyc_world_prop_overlay")
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(boardGradient))
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Authentic MTA / Chalkboard white stripe accent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            spec.subwayLines.forEach { (letter, color) ->
                                Surface(
                                    shape = CircleShape,
                                    color = color,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = letter,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (color == Color(0xFFFCCC0A)) Color.Black else Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = spec.headerTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spec.headerSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_nyc_prop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar objeto de la escena",
                            tint = Color.White
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

                spec.items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.phraseEn,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.detailEs,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.72f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = accent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = item.priceOrTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = { onSpeakPhrase(item.phraseEn) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Escuchar ${item.phraseEn}",
                                        tint = accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onInsertPhrase(item.phraseEn)
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Usar ${item.phraseEn}",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
