package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.TasbihItem
import com.example.data.model.TasbihPresets
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    userPreferences: UserPreferences,
    database: AppDatabase,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val tasbihList by database.tasbihDao().getAllTasbihItems().collectAsState(initial = emptyList())
    val vibrationEnabled by userPreferences.vibrationEnabled.collectAsState()
    val soundClickEnabled by userPreferences.soundClickEnabled.collectAsState()

    var activeTasbih by remember { mutableStateOf<TasbihItem?>(null) }
    var lapCount by remember { mutableIntStateOf(0) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var isPressedAnimation by remember { mutableStateOf(false) }

    // Initialize with first preset if active is null
    LaunchedEffect(tasbihList) {
        if (activeTasbih == null && tasbihList.isNotEmpty()) {
            activeTasbih = tasbihList.first()
        } else if (activeTasbih != null) {
            // Keep active tasbih synced with database updates
            val updated = tasbihList.find { it.id == activeTasbih!!.id }
            if (updated != null) {
                activeTasbih = updated
            }
        }
    }

    val currentItem = activeTasbih ?: TasbihPresets.DEFAULTS.first()
    val currentCount = currentItem.currentCount
    val targetCount = currentItem.targetCount

    // Haptic & Sound Feedback Helper
    fun triggerFeedback(isTargetReached: Boolean) {
        if (vibrationEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    val vibrator = vibratorManager.defaultVibrator
                    if (isTargetReached) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150), -1))
                    } else {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (isTargetReached) {
                        vibrator.vibrate(150)
                    } else {
                        vibrator.vibrate(25)
                    }
                }
            } catch (e: Exception) {
                // Ignore if not supported
            }
        }

        if (soundClickEnabled) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40)
                if (isTargetReached) {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                } else {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun handleIncrement() {
        val nextCount = currentCount + 1
        val reachedTarget = targetCount > 0 && nextCount >= targetCount

        coroutineScope.launch {
            if (reachedTarget) {
                lapCount++
                database.tasbihDao().updateTasbihItem(
                    currentItem.copy(
                        currentCount = 0,
                        totalLifetimeCount = currentItem.totalLifetimeCount + 1
                    )
                )
            } else {
                database.tasbihDao().updateTasbihItem(
                    currentItem.copy(
                        currentCount = nextCount,
                        totalLifetimeCount = currentItem.totalLifetimeCount + 1
                    )
                )
            }
            triggerFeedback(reachedTarget)
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressedAnimation) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "tasbih_button_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tasbih_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Toolbar: Toggles (Sound, Vibration) & Add Custom
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Digital Tasbih",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Vibration Toggle
                    IconButton(
                        onClick = { userPreferences.setTasbihVibration(!vibrationEnabled) },
                        modifier = Modifier.testTag("toggle_vibration_button")
                    ) {
                        Icon(
                            imageVector = if (vibrationEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                            contentDescription = "Toggle Vibration",
                            tint = if (vibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Sound Click Toggle
                    IconButton(
                        onClick = { userPreferences.setTasbihSound(!soundClickEnabled) },
                        modifier = Modifier.testTag("toggle_sound_button")
                    ) {
                        Icon(
                            imageVector = if (soundClickEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Click Sound",
                            tint = if (soundClickEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Add Custom Dhikr Button
                    IconButton(
                        onClick = { showAddCustomDialog = true },
                        modifier = Modifier.testTag("add_custom_dhikr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom Dhikr",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Dhikr Presets Horizontal Scroll
        val allDhikrs = if (tasbihList.isNotEmpty()) tasbihList else TasbihPresets.DEFAULTS
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allDhikrs) { item ->
                val isSelected = activeTasbih?.id == item.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        activeTasbih = item
                        lapCount = 0
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.testTag("dhikr_chip_${item.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dhikr Details Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("active_dhikr_info_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentItem.arabicText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = currentItem.translation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Large Circular Tactile Tasbih Counter Button
        val targetProgress = if (targetCount > 0) currentCount.toFloat() / targetCount.toFloat() else 0f

        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        isPressedAnimation = true
                        handleIncrement()
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(80)
                            isPressedAnimation = false
                        }
                    }
                )
                .testTag("tasbih_counter_button"),
            contentAlignment = Alignment.Center
        ) {
            // Circular Progress Indicator Ring around the button
            if (targetCount > 0) {
                CircularProgressIndicator(
                    progress = { targetProgress },
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$currentCount",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = if (targetCount > 0) "TARGET: $targetCount" else "FREE COUNT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TAP TO COUNT",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Stats Bar (Laps, Lifetime Count, Reset)
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Completed Cycles / Laps
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LAPS COMPLETED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$lapCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Reset Button
                FilledTonalButton(
                    onClick = { showResetConfirmDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.testTag("reset_tasbih_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Count",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }

                // Lifetime Total Dhikr
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOTAL LIFETIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currentItem.totalLifetimeCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Tasbih Counter?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset current count to 0? Your total lifetime count will remain saved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            database.tasbihDao().resetCount(currentItem.id)
                            lapCount = 0
                            showResetConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Custom Dhikr Dialog
    if (showAddCustomDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newArabic by remember { mutableStateOf("") }
        var newTranslation by remember { mutableStateOf("") }
        var newTargetStr by remember { mutableStateOf("33") }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("New Custom Dhikr", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Dhikr Name (e.g. Istighfar)") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newArabic,
                        onValueChange = { newArabic = it },
                        label = { Text("Arabic Text (optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_arabic_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newTranslation,
                        onValueChange = { newTranslation = it },
                        label = { Text("Meaning / Translation (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newTargetStr,
                        onValueChange = { newTargetStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Target Count (0 for unlimited)") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_target_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            coroutineScope.launch {
                                val target = newTargetStr.toIntOrNull() ?: 33
                                val id = database.tasbihDao().insertTasbihItem(
                                    TasbihItem(
                                        title = newTitle.trim(),
                                        arabicText = if (newArabic.isNotBlank()) newArabic.trim() else newTitle.trim(),
                                        translation = newTranslation.trim(),
                                        targetCount = target,
                                        isDefault = false
                                    )
                                )
                                showAddCustomDialog = false
                            }
                        }
                    },
                    enabled = newTitle.isNotBlank()
                ) {
                    Text("Save Dhikr")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
