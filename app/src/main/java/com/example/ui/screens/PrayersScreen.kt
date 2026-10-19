package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserPreferences
import com.example.data.model.*
import com.example.notifications.NotificationHelper
import com.example.ui.components.QiblaCompassDialog
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val city = userPreferences.getSelectedCity()
    val calcMethod by userPreferences.calculationMethod.collectAsState()
    val juristicMethod by userPreferences.juristicMethod.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showQiblaCompass by remember { mutableStateOf(false) }

    // Live clock ticker
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val currentMinutesOfDay = remember(currentTime) {
        currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
    }

    val currentSecondsOfDay = remember(currentTime) {
        currentTime.get(Calendar.HOUR_OF_DAY) * 3600 +
        currentTime.get(Calendar.MINUTE) * 60 +
        currentTime.get(Calendar.SECOND)
    }

    // Refresh notification settings per prayer
    var notifVersion by remember { mutableIntStateOf(0) }
    val notificationSettings = remember(notifVersion) {
        PrayerType.values().associateWith { userPreferences.isPrayerNotificationEnabled(it) }
    }

    val prayerTimes = remember(currentTime, city, calcMethod, juristicMethod, notifVersion) {
        PrayerTimeCalculator.calculatePrayerTimes(
            calendar = currentTime,
            city = city,
            method = calcMethod,
            juristic = juristicMethod,
            notificationSettings = notificationSettings
        )
    }

    val (nextPrayer, totalRemainingSeconds) = remember(prayerTimes, currentSecondsOfDay) {
        val p = prayerTimes.getNextPrayer(currentSecondsOfDay / 60).first
        val targetSec = p.toMinutesOfDay() * 60
        val diff = if (targetSec > currentSecondsOfDay) {
            targetSec - currentSecondsOfDay
        } else {
            (86400 - currentSecondsOfDay) + (prayerTimes.fajr.toMinutesOfDay() * 60)
        }
        p to diff
    }

    val currentPrayer = remember(prayerTimes, currentMinutesOfDay) {
        prayerTimes.getCurrentPrayer(currentMinutesOfDay)
    }

    val todayDow = currentTime.get(Calendar.DAY_OF_WEEK)
    val dayInfo = remember(todayDow) { WeekdayHelper.getDayInfo(todayDow) }
    val isTodayFriday = dayInfo.isFriday

    val liveTimeFmt = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val liveDateFmt = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prayers_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location and Actions Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // City Selector Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showCityDialog = true }
                        .testTag("city_selector_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${city.name}, ${city.country}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    // Qibla Compass Icon Button
                    IconButton(
                        onClick = { showQiblaCompass = true },
                        modifier = Modifier.testTag("qibla_compass_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Qibla Direction",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Calculation Method Settings Button
                    IconButton(
                        onClick = { showMethodDialog = true },
                        modifier = Modifier.testTag("calculation_method_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Calculation Settings"
                        )
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isTodayFriday) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().testTag("prayers_day_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isTodayFriday) "★ " else "📅 ",
                            fontSize = 18.sp
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${dayInfo.nameEn} (AD)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTodayFriday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${dayInfo.nameAr}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (isTodayFriday)
                                    "Sayyid al-Ayyam: The Blessed Day of Jumu'ah"
                                else if (dayInfo.isSunnahFastDay)
                                    "Sunnah Fasting Day (${dayInfo.nameEn})"
                                else
                                    "AD Weekday • ${dayInfo.shortNameEn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isTodayFriday) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = "Jumu'ah",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Real-Time Digital Clock & Real Date Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("real_time_clock_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-TIME & DATE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "${city.name} (${if (city.timezoneOffsetHours >= 0) "UTC+" else "UTC"}${city.timezoneOffsetHours.toInt()})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Digital Clock with seconds
                    Text(
                        text = liveTimeFmt.format(currentTime.time),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Real AD Date and Arabic Day
                    Text(
                        text = liveDateFmt.format(currentTime.time) + " (AD)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Hero Next Prayer Countdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_prayer_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEXT PRAYER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${nextPrayer.type.displayName} • ${nextPrayer.type.displayNameAr}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "at ${nextPrayer.formattedTime12h}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real-Time Seconds Countdown Badge
                    val remHours = totalRemainingSeconds / 3600
                    val remMins = (totalRemainingSeconds % 3600) / 60
                    val remSecs = totalRemainingSeconds % 60
                    val countdownStr = if (remHours > 0) {
                        String.format("in %02dh : %02dm : %02ds", remHours, remMins, remSecs)
                    } else {
                        String.format("in %02dm : %02ds", remMins, remSecs)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = countdownStr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar indicator towards next prayer
                    val totalSpanSec = 240 * 60f // approx 4h between prayers
                    val elapsedSec = (totalSpanSec - totalRemainingSeconds).coerceIn(0f, totalSpanSec)
                    val progressFraction = elapsedSec / totalSpanSec
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // Section Title: Today's Schedule
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Prayer Times",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = calcMethod.title.split(" ").first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // List of all 6 prayers
        items(prayerTimes.allPrayers) { prayer ->
            val isNext = prayer.type == nextPrayer.type
            val isCurrent = prayer.type == currentPrayer.type && !isNext

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_card_${prayer.type.name.lowercase()}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isNext -> MaterialTheme.colorScheme.primaryContainer
                        isCurrent -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(if (isNext) 3.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isNext -> MaterialTheme.colorScheme.primary
                                        isCurrent -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (prayer.type) {
                                    PrayerType.FAJR -> Icons.Default.WbTwilight
                                    PrayerType.SUNRISE -> Icons.Default.WbSunny
                                    PrayerType.DHUHR -> Icons.Default.LightMode
                                    PrayerType.ASR -> Icons.Default.Brightness5
                                    PrayerType.MAGHRIB -> Icons.Default.NightsStay
                                    PrayerType.ISHA -> Icons.Default.Bedtime
                                },
                                contentDescription = null,
                                tint = when {
                                    isNext -> MaterialTheme.colorScheme.onPrimary
                                    isCurrent -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        val isFridayDhuhr = isTodayFriday && prayer.type == PrayerType.DHUHR
                        val displayNameEn = if (isFridayDhuhr) "Jumu'ah Prayer" else prayer.type.displayName
                        val displayNameAr = if (isFridayDhuhr) "صلاة الجمعة" else prayer.type.displayNameAr

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = displayNameEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isNext || isFridayDhuhr) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isFridayDhuhr) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = displayNameAr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isFridayDhuhr) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isFridayDhuhr) {
                                Text(
                                    text = "★ Congregational Prayer & Khutbah",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            } else if (isNext) {
                                Text(
                                    text = "Upcoming next",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.formattedTime12h,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Bell notification toggle
                        val isEnabled = prayer.isNotificationEnabled
                        IconButton(
                            onClick = {
                                val newState = !isEnabled
                                userPreferences.setPrayerNotificationEnabled(prayer.type, newState)
                                notifVersion++
                                if (newState) {
                                    NotificationHelper.showNotification(
                                        context = context,
                                        notificationId = prayer.type.hashCode(),
                                        channelId = NotificationHelper.CHANNEL_PRAYERS_ID,
                                        title = "${prayer.type.displayName} Notification Enabled",
                                        message = "You will receive timely Adhan reminders for ${prayer.type.displayName} at ${prayer.formattedTime12h}."
                                    )
                                }
                            },
                            modifier = Modifier.testTag("bell_toggle_${prayer.type.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = "Toggle notification for ${prayer.type.displayName}",
                                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text("Select Location", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                    items(PrayerTimeCalculator.POPULAR_CITIES.indices.toList()) { idx ->
                        val itemCity = PrayerTimeCalculator.POPULAR_CITIES[idx]
                        val isSelected = city.name == itemCity.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    userPreferences.setSelectedCityIndex(idx)
                                    showCityDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("city_option_${itemCity.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = itemCity.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = itemCity.country,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Calculation Method Settings Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text("Prayer Calculation Settings", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Calculation Authority:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    CalculationMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    userPreferences.setCalculationMethod(method)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = calcMethod == method,
                                onClick = { userPreferences.setCalculationMethod(method) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = method.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Asr Juristic Method:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    JuristicMethod.values().forEach { jMethod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    userPreferences.setJuristicMethod(jMethod)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = juristicMethod == jMethod,
                                onClick = { userPreferences.setJuristicMethod(jMethod) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = jMethod.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Qibla Compass Dialog
    if (showQiblaCompass) {
        QiblaCompassDialog(
            city = city,
            onDismiss = { showQiblaCompass = false }
        )
    }
}
