package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.QuranAudioPlayer
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.HijriCalendarHelper
import com.example.data.model.WeekdayHelper
import com.example.notifications.NotificationHelper
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MainNavTab(val title: String, val icon: ImageVector) {
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    PRAYERS("Prayers", Icons.Default.AccessTimeFilled),
    QURAN("Quran", Icons.Default.AutoStories),
    TASBIH("Tasbih", Icons.Default.Adjust),
    REMINDERS("Reminders", Icons.Default.NotificationsActive)
}

class MainActivity : ComponentActivity() {

    private lateinit var audioPlayer: QuranAudioPlayer
    private lateinit var userPreferences: UserPreferences
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannels(this)

        audioPlayer = QuranAudioPlayer(this)
        userPreferences = UserPreferences(this)
        database = AppDatabase.getInstance(this)

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    audioPlayer = audioPlayer,
                    userPreferences = userPreferences,
                    database = database
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    audioPlayer: QuranAudioPlayer,
    userPreferences: UserPreferences,
    database: AppDatabase
) {
    var selectedTab by remember { mutableStateOf(MainNavTab.CALENDAR) }
    var targetQuranSurah by remember { mutableStateOf<Int?>(null) }

    val hijriAdjustment by userPreferences.hijriAdjustment.collectAsState()
    val city = userPreferences.getSelectedCity()

    var liveCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            liveCalendar = Calendar.getInstance()
        }
    }

    val todayCalendar = liveCalendar
    val todayDow = todayCalendar.get(Calendar.DAY_OF_WEEK)
    val todayDayInfo = remember(todayDow) { WeekdayHelper.getDayInfo(todayDow) }

    val todayHijri = remember(liveCalendar, hijriAdjustment) {
        HijriCalendarHelper.gregorianToHijri(
            todayCalendar.get(Calendar.YEAR),
            todayCalendar.get(Calendar.MONTH) + 1,
            todayCalendar.get(Calendar.DAY_OF_MONTH),
            hijriAdjustment
        )
    }

    val adShortDateFmt = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }
    val liveTimeFmt = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (todayDayInfo.isFriday)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.primary
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (todayDayInfo.isFriday) "★" else "☪",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Muslim Quran",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (todayDayInfo.isFriday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondary
                                    ) {
                                        Text(
                                            text = "Jumu'ah",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${todayDayInfo.nameEn} (AD) • ${todayHijri.day} ${todayHijri.monthNameEn} ${todayHijri.year} AH • ${city.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Live Real-Time clock badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = liveTimeFmt.format(liveCalendar.time),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                MainNavTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                MainNavTab.CALENDAR -> CalendarScreen(
                    userPreferences = userPreferences,
                    onNavigateToQuranSurah = { surahNum ->
                        targetQuranSurah = surahNum
                        selectedTab = MainNavTab.QURAN
                    },
                    onNavigateToTasbih = {
                        selectedTab = MainNavTab.TASBIH
                    }
                )
                MainNavTab.PRAYERS -> PrayersScreen(userPreferences = userPreferences)
                MainNavTab.QURAN -> QuranScreen(
                    audioPlayer = audioPlayer,
                    userPreferences = userPreferences,
                    database = database,
                    initialSurahNumber = targetQuranSurah,
                    onClearInitialSurah = { targetQuranSurah = null }
                )
                MainNavTab.TASBIH -> TasbihScreen(
                    userPreferences = userPreferences,
                    database = database
                )
                MainNavTab.REMINDERS -> RemindersScreen(database = database)
            }
        }
    }
}
