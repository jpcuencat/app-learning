package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferences
import com.example.model.ThemeMode
import com.example.model.VoiceProfile
import com.example.notifications.ReviewReminderScheduler
import com.example.security.BiometricAuthHelper
import com.example.security.BiometricCapability

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    currentVoice: VoiceProfile,
    pendingSyncCount: Int,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    onTestNotification: () -> Boolean,
    onOpenVoiceComparison: () -> Unit,
    onClearProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val biometricCapability = remember(context) { BiometricAuthHelper.checkCapability(context) }
    var notificationStatusMsg by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleReminders(true)
            val sent = onTestNotification()
            notificationStatusMsg = if (sent) {
                "Notificaciones activadas y aviso de prueba enviado."
            } else {
                "Notificaciones activadas."
            }
        } else {
            notificationStatusMsg = "Permiso de notificaciones no concedido."
        }
    }

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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Ajustes",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Personaliza apariencia, seguridad biométrica, recordatorios y almacenamiento local.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dark Mode Configurable
            item {
                SettingsCard(
                    icon = Icons.Filled.DarkMode,
                    title = "Modo oscuro y apariencia",
                    subtitle = "Automático sigue el ajuste del sistema para reducir la fatiga visual."
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { idx, mode ->
                            SegmentedButton(
                                selected = preferences.themeMode == mode,
                                onClick = { onSelectThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = ThemeMode.entries.size
                                ),
                                modifier = Modifier.testTag("theme_mode_${mode.storageKey}")
                            ) {
                                Text(mode.labelEs)
                            }
                        }
                    }
                }
            }

            // Default Voice
            item {
                SettingsCard(
                    icon = Icons.Filled.GraphicEq,
                    title = "Voz predeterminada de las lecciones",
                    subtitle = "${currentVoice.realName} · ${currentVoice.accentLabelEs}"
                ) {
                    FilledTonalButton(
                        onClick = onOpenVoiceComparison,
                        modifier = Modifier.testTag("settings_compare_voices_button")
                    ) {
                        Text("Abrir Comparar voces")
                    }
                }
            }

            // Biometric Fingerprint Lock
            item {
                SettingsCard(
                    icon = Icons.Filled.Fingerprint,
                    title = "Bloqueo con huella dactilar",
                    subtitle = when (biometricCapability) {
                        BiometricCapability.AVAILABLE ->
                            "Protege tu avance solicitando tu huella dactilar al abrir la app."
                        BiometricCapability.NONE_ENROLLED ->
                            "Registra una huella o PIN en los ajustes del dispositivo para activar este bloqueo."
                        BiometricCapability.UNAVAILABLE ->
                            "Este dispositivo no dispone de sensor biométrico compatible."
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (preferences.biometricLockEnabled) "Protección activada" else "Desactivada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = preferences.biometricLockEnabled,
                            enabled = biometricCapability == BiometricCapability.AVAILABLE,
                            onCheckedChange = onToggleBiometric,
                            modifier = Modifier.testTag("biometric_lock_switch")
                        )
                    }
                }
            }

            // Personalized Push / Local Review Notifications
            item {
                SettingsCard(
                    icon = Icons.Filled.NotificationsActive,
                    title = "Notificaciones de repaso",
                    subtitle = "Te avisamos automáticamente cuando llegue la fecha sugerida para repasar una lección."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (preferences.remindersEnabled) "Recordatorios activos" else "Recordatorios pausados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = preferences.remindersEnabled,
                            onCheckedChange = { enable ->
                                if (enable &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !ReviewReminderScheduler.hasNotificationPermission(context)
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onToggleReminders(enable)
                                }
                            },
                            modifier = Modifier.testTag("reminders_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !ReviewReminderScheduler.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val ok = onTestNotification()
                                notificationStatusMsg = if (ok) {
                                    "Notificación de prueba enviada."
                                } else {
                                    "Activa los permisos de notificación del sistema para ver el aviso."
                                }
                            }
                        },
                        modifier = Modifier.testTag("test_notification_button")
                    ) {
                        Text("Enviar aviso de prueba")
                    }

                    if (notificationStatusMsg != null) {
                        Text(
                            text = notificationStatusMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Local-first + Remote Sync Ready Architecture
            item {
                SettingsCard(
                    icon = Icons.Filled.PhoneAndroid,
                    title = "Almacenamiento local y sincronización",
                    subtitle = "Tu avance se guarda en este teléfono para practicar sin conexión. La arquitectura modular está preparada para sincronización remota futura."
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudQueue,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Modo local activo · $pendingSyncCount registros locales guardados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("reset_progress_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reiniciar avance local")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Reiniciar avance local?") },
            text = { Text("Se borrará el turno guardado y las fechas de repaso de todas las lecciones en este dispositivo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearProgress()
                        showResetDialog = false
                    }
                ) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}
