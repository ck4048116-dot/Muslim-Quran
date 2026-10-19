package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserPreferences
import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    userPreferences: UserPreferences,
    onNavigateToQuranSurah: ((Int) -> Unit)? = null,
    onNavigateToTasbih: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hijriAdjustment by userPreferences.hijriAdjustment.collectAsState()
    val city = userPreferences.getSelectedCity()
    val calcMethod by userPreferences.calculationMethod.collectAsState()

    var displayedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    var liveCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            liveCalendar = Calendar.getInstance()
        }
    }

    val liveTimeFmt = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }

    val todayCalendar = liveCalendar
    val todayHijri = remember(liveCalendar, hijriAdjustment) {
        HijriCalendarHelper.gregorianToHijri(
            todayCalendar.get(Calendar.YEAR),
            todayCalendar.get(Calendar.MONTH) + 1,
            todayCalendar.get(Calendar.DAY_OF_MONTH),
            hijriAdjustment
        )
    }

    val selectedHijri = remember(selectedDayCalendar, hijriAdjustment) {
        HijriCalendarHelper.gregorianToHijri(
            selectedDayCalendar.get(Calendar.YEAR),
            selectedDayCalendar.get(Calendar.MONTH) + 1,
            selectedDayCalendar.get(Calendar.DAY_OF_MONTH),
            hijriAdjustment
        )
    }

    val selectedDayPrayers = remember(selectedDayCalendar, city, calcMethod) {
        PrayerTimeCalculator.calculatePrayerTimes(
            calendar = selectedDayCalendar,
            city = city,
            method = calcMethod
        )
    }

    val (moonName, moonEmoji) = remember(selectedHijri) {
        HijriCalendarHelper.getMoonPhaseInfo(selectedHijri.day)
    }

    val todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)
    val todayDayInfo = remember(todayDayOfWeek) { WeekdayHelper.getDayInfo(todayDayOfWeek) }

    val selectedDayOfWeek = selectedDayCalendar.get(Calendar.DAY_OF_WEEK)
    val selectedDayInfo = remember(selectedDayOfWeek) { WeekdayHelper.getDayInfo(selectedDayOfWeek) }

    // Friday Sunnahs Checklist State
    val checkedSunnahs = remember { mutableStateMapOf<String, Boolean>() }

    var showAdjustmentDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Today Card with AD Day & Hijri Date
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_hijri_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (todayDayInfo.isFriday)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Top row: AD Day badge & Live Clock & Hijri offset adjustment chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (todayDayInfo.isFriday)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (todayDayInfo.isFriday) "★ SPECIAL: FRIDAY (الجمعة)" else "TODAY • ${todayDayInfo.nameEn.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (todayDayInfo.isFriday) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // Live Ticking Clock Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = liveTimeFmt.format(liveCalendar.time),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Adjustment chip button
                        SuggestionChip(
                            onClick = { showAdjustmentDialog = true },
                            label = {
                                Text(
                                    text = if (hijriAdjustment == 0) "Offset: 0d" else "Offset: ${if (hijriAdjustment > 0) "+$hijriAdjustment" else "$hijriAdjustment"}d",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Adjust Hijri offset",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("hijri_adjustment_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent AD Day & Gregorian Date
                    val gregorianFmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${todayDayInfo.nameEn} (AD)",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = gregorianFmt.format(todayCalendar.time),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        // Arabic Day Name
                        Text(
                            text = todayDayInfo.nameAr,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Corresponding Hijri Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = todayHijri.formatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = todayHijri.formattedAr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = moonEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = moonName.split("(").first(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (todayDayInfo.specialBadge != null || todayHijri.event != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (todayDayInfo.specialBadge != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (todayDayInfo.isFriday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = todayDayInfo.specialBadge!!,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (todayHijri.event != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = todayHijri.event!!,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SPECIAL FRIDAY SECTION (Sayyid al-Ayyam)
        val isSelectedDayFriday = selectedDayInfo.isFriday
        val isTodayFriday = todayDayInfo.isFriday
        val shouldShowFridaySpecial = isSelectedDayFriday || isTodayFriday

        if (shouldShowFridaySpecial) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friday_special_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Friday Special • جمعة مباركة",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "Sayyid al-Ayyam (سيد الأيام) — Master of All Days",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Blessed Jumu'ah",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hadith quotation
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "«The best day on which the sun has risen is Friday; on it Adam was created, on it he entered Paradise, and on it he was expelled from it.» — Sahih Muslim",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Direct Action Shortcuts: Surah Kahf & Salawat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToQuranSurah?.invoke(18) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("friday_read_kahf_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Surah Al-Kahf", style = MaterialTheme.typography.labelMedium)
                            }

                            FilledTonalButton(
                                onClick = { onNavigateToTasbih?.invoke() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("friday_salawat_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Adjust, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salawat (ﷺ)", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Friday Sunnahs Checklist
                        Text(
                            text = "Friday Sunnah Checklist (سنن وآداب يوم الجمعة)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        WeekdayHelper.FRIDAY_SUNNAHS.forEach { sunnah ->
                            val isChecked = checkedSunnahs[sunnah.id] ?: false
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { checkedSunnahs[sunnah.id] = !isChecked }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checkedSunnahs[sunnah.id] = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = sunnah.titleEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Bold,
                                            color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sunnah.titleAr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "${sunnah.description} (${sunnah.hadithRef})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Monthly Interactive Calendar Grid Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_calendar_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val prev = displayedCalendar.clone() as Calendar
                                prev.add(Calendar.MONTH, -1)
                                displayedCalendar = prev
                            },
                            modifier = Modifier.testTag("prev_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }

                        val monthHeaderFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = monthHeaderFormat.format(displayedCalendar.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val midMonthHijri = HijriCalendarHelper.gregorianToHijri(
                                displayedCalendar.get(Calendar.YEAR),
                                displayedCalendar.get(Calendar.MONTH) + 1,
                                15,
                                hijriAdjustment
                            )
                            Text(
                                text = "${midMonthHijri.monthNameEn} ${midMonthHijri.year} AH",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = {
                                val next = displayedCalendar.clone() as Calendar
                                next.add(Calendar.MONTH, 1)
                                displayedCalendar = next
                            },
                            modifier = Modifier.testTag("next_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AD Day of Week Header row with Sunday to Saturday & Special Friday badge
                    val dayNames = listOf(
                        "Sun" to "أحد",
                        "Mon" to "إثنين",
                        "Tue" to "ثلاثاء",
                        "Wed" to "أربعاء",
                        "Thu" to "خميس",
                        "Fri ★" to "جمعة",
                        "Sat" to "سبت"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        dayNames.forEachIndexed { idx, (en, ar) ->
                            val isFriday = idx == 5
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = en,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isFriday) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isFriday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = ar,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (isFriday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days grid computation
                    val cal = displayedCalendar.clone() as Calendar
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based Sunday
                    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                    var dayCounter = 1
                    val weeksCount = kotlin.math.ceil((firstDayOfWeek + maxDaysInMonth) / 7.0).toInt()

                    for (week in 0 until weeksCount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (dayOfWeek in 0..6) {
                                val cellIndex = week * 7 + dayOfWeek
                                if (cellIndex >= firstDayOfWeek && dayCounter <= maxDaysInMonth) {
                                    val currentDayNum = dayCounter
                                    val cellCal = displayedCalendar.clone() as Calendar
                                    cellCal.set(Calendar.DAY_OF_MONTH, currentDayNum)

                                    val isToday = cellCal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                                            cellCal.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                                            currentDayNum == todayCalendar.get(Calendar.DAY_OF_MONTH)

                                    val isSelected = cellCal.get(Calendar.YEAR) == selectedDayCalendar.get(Calendar.YEAR) &&
                                            cellCal.get(Calendar.MONTH) == selectedDayCalendar.get(Calendar.MONTH) &&
                                            currentDayNum == selectedDayCalendar.get(Calendar.DAY_OF_MONTH)

                                    val cellHijri = HijriCalendarHelper.gregorianToHijri(
                                        cellCal.get(Calendar.YEAR),
                                        cellCal.get(Calendar.MONTH) + 1,
                                        currentDayNum,
                                        hijriAdjustment
                                    )

                                    val isFriday = dayOfWeek == 5 // Friday column

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    isFriday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                                    cellHijri.event != null -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = when {
                                                    isSelected -> 0.dp
                                                    isToday -> 1.5.dp
                                                    isFriday -> 1.dp
                                                    else -> 0.dp
                                                },
                                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                selectedDayCalendar = cellCal
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = currentDayNum.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday || isFriday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                                        isFriday -> MaterialTheme.colorScheme.secondary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                if (isFriday && !isSelected) {
                                                    Spacer(modifier = Modifier.width(1.dp))
                                                    Text("★", fontSize = 8.sp, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            }
                                            Text(
                                                text = "${cellHijri.day}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                                    isFriday -> MaterialTheme.colorScheme.secondary
                                                    cellHijri.event != null -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }

                                        if (cellHijri.event != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-3).dp, y = 3.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondary)
                                            )
                                        }
                                    }
                                    dayCounter++
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details & Prayer Times Preview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_day_details_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    val dateFmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dateFmt.format(selectedDayCalendar.time) + " (AD)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selectedDayInfo.isFriday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "★ Friday",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Text(
                                text = "${selectedHijri.formatted} (${selectedHijri.monthNameAr}) • ${selectedDayInfo.nameAr}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (selectedHijri.event != null) {
                            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                Text(
                                    text = selectedHijri.event!!,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedDayInfo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Prayer Times for ${city.name} (${if (selectedDayInfo.isFriday) "Friday Prayer Day" else "Daily"}):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        selectedDayPrayers.allPrayers.forEach { p ->
                            val prayerDisplayName = if (selectedDayInfo.isFriday && p.type == PrayerType.DHUHR) {
                                "Jumu'ah"
                            } else {
                                p.type.displayName
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = prayerDisplayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedDayInfo.isFriday && p.type == PrayerType.DHUHR) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedDayInfo.isFriday && p.type == PrayerType.DHUHR) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = p.formattedTime12h.replace(" ", "\n"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // AD Days of the Week Guide Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_days_guide_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "AD Days of the Week (أيام الأسبوع)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Islamic significance of Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, and Saturday",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    WeekdayHelper.DAYS.forEach { dayInfo ->
                        val isDaySelected = selectedDayOfWeek == dayInfo.dayOfWeek
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        dayInfo.isFriday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                        isDaySelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else -> Color.Transparent
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (dayInfo.isFriday) MaterialTheme.colorScheme.secondary
                                        else if (dayInfo.isSunnahFastDay) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayInfo.shortNameEn.first().toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dayInfo.isFriday || dayInfo.isSunnahFastDay) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dayInfo.nameEn,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dayInfo.isFriday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (dayInfo.isFriday) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondary
                                            ) {
                                                Text(
                                                    text = "★ Special",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (dayInfo.isSunnahFastDay) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = "Fast",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = dayInfo.nameAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayInfo.isFriday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = dayInfo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        if (dayInfo.dayOfWeek != Calendar.SATURDAY) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Islamic Events & Sacred Dates
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Islamic Events & Holidays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Major observances throughout the Hijri Year",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(HijriCalendarHelper.ISLAMIC_EVENTS) { event ->
            val monthName = HijriCalendarHelper.ISLAMIC_MONTHS_EN.getOrElse(event.hijriMonth - 1) { "" }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_item_${event.nameEn.replace(" ", "_").lowercase()}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (event.isMajorHoliday)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${event.hijriDay}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (event.isMajorHoliday)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.nameEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = event.nameAr,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${event.hijriDay} $monthName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Hijri Adjustment Dialog
    if (showAdjustmentDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustmentDialog = false },
            title = {
                Text("Hijri Date Adjustment", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Because local moon sightings vary by 1–2 days between regions, you can fine-tune the Hijri date offset to match your local religious authority.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(-2, -1, 0, 1, 2).forEach { offset ->
                            FilterChip(
                                selected = hijriAdjustment == offset,
                                onClick = {
                                    userPreferences.setHijriAdjustment(offset)
                                    showAdjustmentDialog = false
                                },
                                label = {
                                    Text(text = if (offset > 0) "+$offset d" else "$offset d")
                                },
                                modifier = Modifier.testTag("offset_chip_$offset")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdjustmentDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
