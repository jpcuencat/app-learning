package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.LessonCatalog
import com.example.data.local.UserPreferences
import com.example.domain.SpacedRepetitionScheduler
import com.example.model.CefrLevel
import com.example.model.Lesson
import com.example.model.LessonProgressSummary
import com.example.model.PlayerRank
import com.example.model.PocketPhraseCard
import com.example.model.SessionMode
import com.example.model.VoiceProfile
import com.example.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    lessons: List<Lesson>,
    selectedLevel: CefrLevel,
    progressMap: Map<String, LessonProgressSummary>,
    preferences: UserPreferences,
    currentVoice: VoiceProfile,
    isOnline: Boolean,
    onSelectLevel: (CefrLevel) -> Unit,
    onOpenLesson: (lessonId: String, mode: SessionMode, resumeSaved: Boolean) -> Unit,
    onStartBlitz: () -> Unit,
    onPlayPocketPhrase: (String) -> Unit,
    onOpenVoiceComparison: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedLessonId by rememberSaveable { mutableStateOf<String?>(null) }
    var showPhraseDeckSheet by rememberSaveable { mutableStateOf(false) }

    val levelLessons = remember(lessons, selectedLevel) {
        lessons.filter { it.level == selectedLevel }
    }
    val playerRank = remember(preferences.totalXp) {
        PlayerRank.fromTotalXp(preferences.totalXp)
    }
    val allCards = LessonCatalog.pocketPhraseCards
    val unlockedCount = preferences.unlockedPhraseIds.size

    // Determine which mission stop is the "next active" stop on the map
    val activeMissionId = remember(levelLessons, progressMap) {
        levelLessons.firstOrNull { (progressMap[it.id]?.practiceTurnIndex ?: 0) > 0 }?.id
            ?: levelLessons.firstOrNull { (progressMap[it.id]?.practiceCompletedCount ?: 0) == 0 }?.id
            ?: levelLessons.firstOrNull()?.id
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isExpanded = maxWidth >= 760.dp
        val selectedDetailLesson = remember(inspectedLessonId, levelLessons, isExpanded) {
            lessons.firstOrNull { it.id == inspectedLessonId }
                ?: if (isExpanded) levelLessons.firstOrNull() else null
        }

        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                MissionMapColumn(
                    lessons = lessons,
                    levelLessons = levelLessons,
                    selectedLevel = selectedLevel,
                    progressMap = progressMap,
                    preferences = preferences,
                    playerRank = playerRank,
                    currentVoice = currentVoice,
                    isOnline = isOnline,
                    activeMissionId = activeMissionId,
                    selectedLessonId = selectedDetailLesson?.id,
                    unlockedCount = unlockedCount,
                    totalCardsCount = allCards.size,
                    onSelectLevel = onSelectLevel,
                    onInspectLesson = { inspectedLessonId = it.id },
                    onStartBlitz = onStartBlitz,
                    onOpenPhraseDeck = { showPhraseDeckSheet = true },
                    onOpenVoiceComparison = onOpenVoiceComparison,
                    modifier = Modifier.weight(1.1f)
                )

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                ) {
                    if (selectedDetailLesson != null) {
                        MissionBriefingSheetContent(
                            lesson = selectedDetailLesson,
                            progress = progressMap[selectedDetailLesson.id],
                            isCardUnlocked = "card_${selectedDetailLesson.id}" in preferences.unlockedPhraseIds,
                            onPlayRewardPhrase = { onPlayPocketPhrase(selectedDetailLesson.rewardPhraseEn) },
                            onOpenLesson = { mode, resume ->
                                onOpenLesson(selectedDetailLesson.id, mode, resume)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        )
                    }
                }
            }
        } else {
            MissionMapColumn(
                lessons = lessons,
                levelLessons = levelLessons,
                selectedLevel = selectedLevel,
                progressMap = progressMap,
                preferences = preferences,
                playerRank = playerRank,
                currentVoice = currentVoice,
                isOnline = isOnline,
                activeMissionId = activeMissionId,
                selectedLessonId = inspectedLessonId,
                unlockedCount = unlockedCount,
                totalCardsCount = allCards.size,
                onSelectLevel = onSelectLevel,
                onInspectLesson = { inspectedLessonId = it.id },
                onStartBlitz = onStartBlitz,
                onOpenPhraseDeck = { showPhraseDeckSheet = true },
                onOpenVoiceComparison = onOpenVoiceComparison,
                modifier = Modifier.fillMaxSize()
            )

            // Instant Mission Briefing Bottom Sheet
            val sheetLesson = lessons.firstOrNull { it.id == inspectedLessonId }
            if (sheetLesson != null) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { inspectedLessonId = null },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    MissionBriefingSheetContent(
                        lesson = sheetLesson,
                        progress = progressMap[sheetLesson.id],
                        isCardUnlocked = "card_${sheetLesson.id}" in preferences.unlockedPhraseIds,
                        onPlayRewardPhrase = { onPlayPocketPhrase(sheetLesson.rewardPhraseEn) },
                        onOpenLesson = { mode, resume ->
                            inspectedLessonId = null
                            onOpenLesson(sheetLesson.id, mode, resume)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                    )
                }
            }
        }

        // Collectible Pocket Phrase Deck Modal Sheet
        if (showPhraseDeckSheet) {
            val deckSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showPhraseDeckSheet = false },
                sheetState = deckSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                PocketPhraseDeckSheet(
                    allCards = allCards,
                    unlockedIds = preferences.unlockedPhraseIds,
                    onPlayPhrase = onPlayPocketPhrase
                )
            }
        }
    }
}

@Composable
private fun MissionMapColumn(
    lessons: List<Lesson>,
    levelLessons: List<Lesson>,
    selectedLevel: CefrLevel,
    progressMap: Map<String, LessonProgressSummary>,
    preferences: UserPreferences,
    playerRank: PlayerRank,
    currentVoice: VoiceProfile,
    isOnline: Boolean,
    activeMissionId: String?,
    selectedLessonId: String?,
    unlockedCount: Int,
    totalCardsCount: Int,
    onSelectLevel: (CefrLevel) -> Unit,
    onInspectLesson: (Lesson) -> Unit,
    onStartBlitz: () -> Unit,
    onOpenPhraseDeck: () -> Unit,
    onOpenVoiceComparison: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Player Game HUD Bar (Streak, XP Rank, Voice shortcut)
        item {
            PlayerGameHudHeader(
                streakDays = preferences.currentStreakDays,
                totalXp = preferences.totalXp,
                playerRank = playerRank,
                currentVoiceName = currentVoice.realName.substringBefore(" ·"),
                isOnline = isOnline,
                onOpenVoiceComparison = onOpenVoiceComparison
            )
        }

        // 2. Arcade Mini-Game Banner: Reto Relámpago 60s
        item {
            LightningBlitzBannerCard(
                bestScore = preferences.blitzBestScore,
                onPlayBlitz = onStartBlitz
            )
        }

        // 3. CEFR Level Route Switcher + Collectible Phrase Deck button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(CefrLevel.entries) { level ->
                        val isSelected = level == selectedLevel
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectLevel(level) },
                            modifier = Modifier.testTag("level_chip_${level.code.lowercase()}"),
                            label = {
                                Text(
                                    text = level.fullBadge,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    onClick = onOpenPhraseDeck,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.testTag("pocket_phrases_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Style,
                            contentDescription = "Frases de bolsillo",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$unlockedCount/$totalCardsCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Interactive Winding City Route Map — ALL stops visible at a glance!
        item {
            CityRouteMapCard(
                level = selectedLevel,
                levelLessons = levelLessons,
                progressMap = progressMap,
                activeMissionId = activeMissionId,
                selectedLessonId = selectedLessonId,
                onInspectLesson = onInspectLesson
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }
    }
}

@Composable
private fun PlayerGameHudHeader(
    streakDays: Int,
    totalXp: Int,
    playerRank: PlayerRank,
    currentVoiceName: String,
    isOnline: Boolean,
    onOpenVoiceComparison: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HablaYa",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Nv.${playerRank.levelNumber} ${playerRank.titleEs}" +
                        if (isOnline) " · Offline + IA" else " · Offline",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Streak pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Racha diaria",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${streakDays}d",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // XP pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Puntos XP",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "$totalXp XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Compact Voice Comparison button
                Surface(
                    onClick = onOpenVoiceComparison,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("compare_voices_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = "Comparar voces",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = currentVoiceName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        LinearProgressIndicator(
            progress = { playerRank.progressFraction.coerceAtLeast(0.05f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
private fun LightningBlitzBannerCard(
    bestScore: Int,
    onPlayBlitz: () -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Card(
        onClick = onPlayBlitz,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lightning_blitz_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Reto Relámpago",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reto Relámpago · 60 s",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (bestScore > 0) {
                            "3 vidas · Combos x4 · Récord: $bestScore pts"
                        } else {
                            "3 vidas · Responde contra reloj y sube de rango"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White
                ) {
                    Text(
                        text = "Jugar",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CityRouteMapCard(
    level: CefrLevel,
    levelLessons: List<Lesson>,
    progressMap: Map<String, LessonProgressSummary>,
    activeMissionId: String?,
    selectedLessonId: String?,
    onInspectLesson: (Lesson) -> Unit
) {
    val totalStarsPossible = levelLessons.size * 3
    val earnedStars = levelLessons.sumOf { progressMap[it.id]?.starsEarned ?: 0 }
    val routeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val completedRouteColor = MaterialTheme.colorScheme.primary
    val rows = remember(levelLessons) { levelLessons.chunked(2) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Map Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mapa · ${level.fullBadge}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = level.descriptionEs,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Estrellas del nivel",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$earnedStars/$totalStarsPossible",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Winding City Route Canvas + Interactive Mission Pins (All stops fit cleanly in 1 view!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        )
                    )
                    .padding(vertical = 18.dp, horizontal = 10.dp)
            ) {
                // Connecting winding road behind the mission pins
                Canvas(modifier = Modifier.matchParentSize()) {
                    if (rows.isEmpty()) return@Canvas
                    val rowHeight = size.height / rows.size
                    val leftX = size.width * 0.25f
                    val rightX = size.width * 0.75f
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)

                    val path = Path()
                    val points = mutableListOf<Offset>()
                    rows.forEachIndexed { rowIdx, rowItems ->
                        val y = rowHeight * rowIdx + rowHeight * 0.42f
                        val isReversed = rowIdx % 2 == 1
                        if (rowItems.size == 1) {
                            points.add(Offset(size.width * 0.5f, y))
                        } else {
                            if (!isReversed) {
                                points.add(Offset(leftX, y))
                                points.add(Offset(rightX, y))
                            } else {
                                points.add(Offset(rightX, y))
                                points.add(Offset(leftX, y))
                            }
                        }
                    }

                    if (points.size >= 2) {
                        path.moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            if (prev.y == curr.y) {
                                path.lineTo(curr.x, curr.y)
                            } else {
                                val midY = (prev.y + curr.y) / 2f
                                path.cubicTo(prev.x, midY, curr.x, midY, curr.x, curr.y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = routeColor,
                            style = Stroke(width = 10f, cap = StrokeCap.Round, pathEffect = dashEffect)
                        )

                        // Highlight completed trail segment
                        val completedStops = levelLessons.count {
                            (progressMap[it.id]?.practiceCompletedCount ?: 0) > 0
                        }
                        if (completedStops >= 1 && points.size >= 2) {
                            val litPath = Path()
                            val litEnd = completedStops.coerceAtMost(points.lastIndex)
                            litPath.moveTo(points.first().x, points.first().y)
                            for (i in 1..litEnd) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                if (prev.y == curr.y) {
                                    litPath.lineTo(curr.x, curr.y)
                                } else {
                                    val midY = (prev.y + curr.y) / 2f
                                    litPath.cubicTo(prev.x, midY, curr.x, midY, curr.x, curr.y)
                                }
                            }
                            drawPath(
                                path = litPath,
                                color = completedRouteColor,
                                style = Stroke(width = 8f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Mission Stops laid out in winding snake order (Row 0: L->R, Row 1: R->L)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    rows.forEachIndexed { rowIdx, rowItems ->
                        val isReversed = rowIdx % 2 == 1
                        val visualRow = if (isReversed && rowItems.size == 2) rowItems.reversed() else rowItems

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top
                        ) {
                            visualRow.forEach { lesson ->
                                MissionNodePin(
                                    lesson = lesson,
                                    progress = progressMap[lesson.id],
                                    isActiveNext = lesson.id == activeMissionId,
                                    isSelected = lesson.id == selectedLessonId,
                                    onClick = { onInspectLesson(lesson) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionNodePin(
    lesson: Lesson,
    progress: LessonProgressSummary?,
    isActiveNext: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val stars = progress?.starsEarned ?: 0
    val hasTurnInProgress = (progress?.practiceTurnIndex ?: 0) > 0
    val reviewDue = SpacedRepetitionScheduler.isReviewDue(progress?.nextReviewEpochDay)
    val isCompleted = (progress?.practiceCompletedCount ?: 0) > 0

    val pulseTransition = rememberInfiniteTransition(label = "node_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActiveNext) 1.07f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringColor = when {
        lesson.isBossMission -> MaterialTheme.colorScheme.tertiary
        isSelected -> MaterialTheme.colorScheme.secondary
        isCompleted -> semantic.success
        isActiveNext -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val statusChipText = when {
        hasTurnInProgress -> "Turno ${(progress?.practiceTurnIndex ?: 0) + 1}"
        reviewDue -> "Repasar hoy"
        lesson.isBossMission && !isCompleted -> "Imprevisto"
        isActiveNext -> "¡Jugar!"
        isCompleted -> "Listo"
        else -> "${lesson.practice.turns.size} turnos"
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("lesson_card_${lesson.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Outer pulsing aura for active mission
            if (isActiveNext || lesson.isBossMission) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(ringColor.copy(alpha = 0.22f))
                )
            }

            // Circular scene artwork framed in thick game ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(width = 3.5.dp, color = ringColor, shape = CircleShape)
                    .padding(3.dp)
            ) {
                Image(
                    painter = painterResource(id = lesson.imageRes),
                    contentDescription = lesson.titleEs,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )

                // Corner icon badge (Boss trophy / Completed check / Play)
                Surface(
                    shape = CircleShape,
                    color = ringColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                lesson.isBossMission -> Icons.Filled.EmojiEvents
                                isCompleted -> Icons.Filled.CheckCircle
                                else -> Icons.Filled.PlayArrow
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 3 Stars Row
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(3) { idx ->
                val earned = idx < stars
                Icon(
                    imageVector = if (earned) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = if (earned) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Stop Title
        Text(
            text = lesson.mapStopLabelEs,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Compact Status Pill
        Surface(
            shape = RoundedCornerShape(50),
            color = when {
                lesson.isBossMission -> MaterialTheme.colorScheme.tertiaryContainer
                isActiveNext || hasTurnInProgress -> MaterialTheme.colorScheme.primary
                reviewDue -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ) {
            Text(
                text = statusChipText,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    lesson.isBossMission -> MaterialTheme.colorScheme.onTertiaryContainer
                    isActiveNext || hasTurnInProgress -> MaterialTheme.colorScheme.onPrimary
                    reviewDue -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionBriefingSheetContent(
    lesson: Lesson,
    progress: LessonProgressSummary?,
    isCardUnlocked: Boolean,
    onPlayRewardPhrase: () -> Unit,
    onOpenLesson: (mode: SessionMode, resumeSaved: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    var previewMode by remember(lesson.id) { mutableStateOf(SessionMode.PRACTICE) }

    val totalTurns = lesson.practice.turns.size
    val savedPracticeIndex = (progress?.practiceTurnIndex ?: 0).coerceIn(0, totalTurns - 1)
    val hasPracticeInProgress = (progress?.practiceTurnIndex ?: 0) > 0
    val hasReviewInProgress = (progress?.reviewTurnIndex ?: 0) > 0
    val stars = progress?.starsEarned ?: 0
    val nextReviewLabel = progress?.nextReviewEpochDay?.let {
        SpacedRepetitionScheduler.formatSuggestedReviewLabelEs(it)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Scene artwork + Boss / Level + Stars
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
                .clip(MaterialTheme.shapes.large)
        ) {
            Image(
                painter = painterResource(id = lesson.imageRes),
                contentDescription = lesson.titleEs,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (lesson.isBossMission) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    }
                ) {
                    Text(
                        text = if (lesson.isBossMission) "Misión Jefe · ${lesson.level.code}" else lesson.level.fullBadge,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (lesson.isBossMission) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (nextReviewLabel != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = semantic.successContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = semantic.success,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Repaso: $nextReviewLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = semantic.success,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = lesson.titleEs,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    repeat(3) { idx ->
                        Icon(
                            imageVector = if (idx < stars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (idx < stars) Color(0xFFFFD166) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (stars == 3) "¡Misión perfecta!" else "1★ Completar · 2★ Fluido · 3★ Sin pistas",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Practice vs Review Variant Switcher
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = previewMode == SessionMode.PRACTICE,
                onClick = { previewMode = SessionMode.PRACTICE },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Misión (${lesson.practice.turns.size} turnos)")
            }
            SegmentedButton(
                selected = previewMode == SessionMode.REVIEW,
                onClick = { previewMode = SessionMode.REVIEW },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Repaso · Variante")
            }
        }

        val activeVariant = if (previewMode == SessionMode.PRACTICE) lesson.practice else lesson.review

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = activeVariant.titleEs,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (previewMode == SessionMode.PRACTICE) lesson.practiceSummaryEs else lesson.reviewSummaryEs,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reward Pocket Phrase Preview
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCardUnlocked) "Frase de bolsillo desbloqueada" else "Recompensa al completar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "“${lesson.rewardPhraseEn}”",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (isCardUnlocked) {
                    IconButton(onClick = onPlayRewardPhrase) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Escuchar frase recompensa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Bloqueada",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasPracticeInProgress) {
                Button(
                    onClick = { onOpenLesson(SessionMode.PRACTICE, true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("continue_turn_${lesson.id}"),
                    contentPadding = PaddingValues(vertical = 13.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Continuar en el turno ${savedPracticeIndex + 1}")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasPracticeInProgress) {
                    OutlinedButton(
                        onClick = { onOpenLesson(SessionMode.PRACTICE, false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_practice_${lesson.id}"),
                        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 8.dp)
                    ) {
                        Text("Empezar práctica", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    Button(
                        onClick = { onOpenLesson(SessionMode.PRACTICE, false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_practice_${lesson.id}"),
                        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Empezar práctica", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                FilledTonalButton(
                    onClick = { onOpenLesson(SessionMode.REVIEW, hasReviewInProgress) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(if (hasReviewInProgress) "continue_review_${lesson.id}" else "review_now_${lesson.id}"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = PaddingValues(vertical = 13.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasReviewInProgress) "Continuar repaso" else "Repasar ahora",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PocketPhraseDeckSheet(
    allCards: List<PocketPhraseCard>,
    unlockedIds: Set<String>,
    onPlayPhrase: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Frases de bolsillo (${unlockedIds.size}/${allCards.size})",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Completa misiones en el mapa para coleccionar frases clave y escucharlas al instante.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allCards, key = { it.id }) { card ->
                val unlocked = card.id in unlockedIds
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (unlocked) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${card.level.code} · ${card.categoryEs}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (unlocked) "“${card.phraseEn}”" else "Completa ${card.categoryEs} para desbloquear",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (unlocked) {
                                Text(
                                    text = card.meaningEs,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (unlocked) {
                            IconButton(onClick = { onPlayPhrase(card.phraseEn) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Escuchar frase",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Bloqueada",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
