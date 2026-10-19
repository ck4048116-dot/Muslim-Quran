package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.model.DefaultReminders
import com.example.data.model.ReminderItem
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    database: AppDatabase,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val remindersList by database.reminderDao().getAllReminders().collectAsState(initial = emptyList())

    // Notification permission check for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    var selectedReminderForTimeEdit by remember { mutableStateOf<ReminderItem?>(null) }
    var testNotifSentMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reminders_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Permission Banner if not granted
        if (!hasNotificationPermission) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("permission_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notifications Disabled",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Grant permission to receive timely Adhan and daily Quran alerts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("grant_notification_button")
                        ) {
                            Text("Allow")
                        }
                    }
                }
            }
        }

        // Test Notification Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("test_notification_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Test Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Send an immediate test alert to verify audio & notification settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = {
                                NotificationHelper.showNotification(
                                    context = context,
                                    notificationId = 9999,
                                    channelId = NotificationHelper.CHANNEL_REMINDERS_ID,
                                    title = "Noor: Daily Quran & Adhkar Alert",
                                    message = "وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا\nIt's time for your daily Quran recitation and remembrance.",
                                    subText = "Verified Alert"
                                )
                                testNotifSentMessage = "Test notification dispatched to your status bar!"
                            },
                            modifier = Modifier.testTag("send_test_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Now")
                        }
                    }

                    if (testNotifSentMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = testNotifSentMessage!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Column {
                Text(
                    text = "Daily Spiritual Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap on any time badge to customize reminder hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reminders List
        val displayList = if (remindersList.isNotEmpty()) remindersList else DefaultReminders.LIST
        items(displayList) { reminder ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_card_${reminder.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (reminder.isEnabled)
                        MaterialTheme.colorScheme.surface
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(if (reminder.isEnabled) 2.dp else 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (reminder.isFridayOnly) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Fridays",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = reminder.arabicPhrase,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = reminder.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Clickable Time Badge to change time
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedReminderForTimeEdit = reminder }
                                .testTag("time_chip_${reminder.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Edit Time",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = reminder.formattedTime,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Switch Toggle
                    Switch(
                        checked = reminder.isEnabled,
                        onCheckedChange = { newState ->
                            coroutineScope.launch {
                                database.reminderDao().toggleReminder(reminder.id, newState)
                                val updated = reminder.copy(isEnabled = newState)
                                if (newState) {
                                    NotificationHelper.scheduleDailyReminder(context, updated)
                                } else {
                                    NotificationHelper.cancelReminder(context, reminder.id)
                                }
                            }
                        },
                        modifier = Modifier.testTag("reminder_switch_${reminder.id}")
                    )
                }
            }
        }
    }

    // Time Selection Dialog
    if (selectedReminderForTimeEdit != null) {
        val rem = selectedReminderForTimeEdit!!
        var editHour by remember { mutableIntStateOf(rem.hour) }
        var editMinute by remember { mutableIntStateOf(rem.minute) }

        AlertDialog(
            onDismissRequest = { selectedReminderForTimeEdit = null },
            title = { Text("Set Reminder Time", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = rem.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hour selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { editHour = (editHour + 1) % 24 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Hour Up")
                            }
                            Text(
                                text = String.format("%02d", editHour),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { editHour = if (editHour == 0) 23 else editHour - 1 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Hour Down")
                            }
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Minute selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { editMinute = (editMinute + 5) % 60 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Minute Up")
                            }
                            Text(
                                text = String.format("%02d", editMinute),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { editMinute = if (editMinute < 5) 55 else editMinute - 5 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minute Down")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val period = if (editHour >= 12) "PM" else "AM"
                    val displayH = when {
                        editHour == 0 -> 12
                        editHour > 12 -> editHour - 12
                        else -> editHour
                    }
                    Text(
                        text = String.format("Will remind at %02d:%02d %s daily", displayH, editMinute, period),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            database.reminderDao().updateTime(rem.id, editHour, editMinute)
                            val updated = rem.copy(hour = editHour, minute = editMinute)
                            if (updated.isEnabled) {
                                NotificationHelper.scheduleDailyReminder(context, updated)
                            }
                            selectedReminderForTimeEdit = null
                        }
                    }
                ) {
                    Text("Save Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReminderForTimeEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
