package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LessonCatalog
import com.example.model.BlindVoiceSample
import com.example.speech.SpeechPlaybackState
import com.example.ui.components.AnimatedAudioWave
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.viewmodel.VoiceComparisonState

@Composable
fun VoiceComparisonScreen(
    comparisonState: VoiceComparisonState,
    speechState: SpeechPlaybackState,
    activeDefaultVoiceId: String,
    onPlaySample: (BlindVoiceSample) -> Unit,
    onStopAudio: () -> Unit,
    onChooseFavorite: (BlindVoiceSample) -> Unit,
    onReshuffle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val listenedCount = comparisonState.playedVoiceIds.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Comparar voces",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Escucha la misma frase en tres voces en orden aleatorio. Cuando elijas tu favorita, revelaremos los nombres y la usaremos como voz predeterminada en tus lecciones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sample phrase card
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Hearing,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Frase de prueba · $listenedCount de 3 escuchadas",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "“${LessonCatalog.VOICE_COMPARISON_SAMPLE_EN}”",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = LessonCatalog.VOICE_COMPARISON_SAMPLE_ES,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            items(comparisonState.samples, key = { it.profile.id }) { sample ->
                val sampleTag = "voice_${sample.profile.id}"
                val isPlayingThis = speechState.isSpeaking && speechState.activeTag == sampleTag
                val isListened = sample.profile.id in comparisonState.playedVoiceIds
                val isChosen = comparisonState.isRevealed &&
                    comparisonState.selectedVoiceId == sample.profile.id
                val isCurrentDefault = comparisonState.isRevealed &&
                    activeDefaultVoiceId == sample.profile.id

                val borderColor by animateColorAsState(
                    targetValue = when {
                        isChosen -> semantic.success
                        isPlayingThis -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    },
                    label = "voice_card_border"
                )
                val elevation by animateDpAsState(
                    targetValue = if (isPlayingThis || isChosen) 6.dp else 1.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "voice_card_elevation"
                )

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChosen) {
                            semantic.successContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(if (isPlayingThis || isChosen) 2.dp else 1.dp, borderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_card_${sample.blindLabel.lowercase().replace(" ", "_")}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isPlayingThis) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = sample.blindLabel.last().toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = if (isPlayingThis) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = sample.blindLabel,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when {
                                            isPlayingThis -> "Reproduciendo muestra…"
                                            isListened -> "Escuchada"
                                            else -> "Toca Escuchar para probarla"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isPlayingThis) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            AnimatedAudioWave(
                                isActive = isPlayingThis,
                                barColor = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Reveal panel once the user picks "Prefiero esta"
                        AnimatedVisibility(
                            visible = comparisonState.isRevealed,
                            enter = fadeIn() + expandVertically()
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isChosen || isCurrentDefault) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Voz elegida",
                                                tint = semantic.success,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = sample.profile.realName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${sample.profile.accentLabelEs} · ${sample.profile.styleDescriptionEs}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (isPlayingThis) onStopAudio() else onPlaySample(sample)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("play_${sample.blindLabel.lowercase().replace(" ", "_")}"),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingThis) {
                                        Icons.Filled.Stop
                                    } else {
                                        Icons.AutoMirrored.Filled.VolumeUp
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPlayingThis) "Detener" else "Escuchar")
                            }

                            Button(
                                onClick = { onChooseFavorite(sample) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prefer_${sample.blindLabel.lowercase().replace(" ", "_")}"),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isChosen) {
                                        Icons.Filled.Favorite
                                    } else {
                                        Icons.Outlined.FavoriteBorder
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isChosen) "Voz activa" else "Prefiero esta")
                            }
                        }
                    }
                }
            }

            if (comparisonState.isRevealed) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = semantic.successContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "¡Nombres revelados y voz guardada!",
                                style = MaterialTheme.typography.titleMedium,
                                color = semantic.success,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tus lecciones usarán ahora tu voz elegida como predeterminada. Puedes volver a mezclar el orden y comparar cuando quieras.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedButton(
                                onClick = onReshuffle,
                                modifier = Modifier.testTag("reshuffle_voices_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Comparar de nuevo en otro orden")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
