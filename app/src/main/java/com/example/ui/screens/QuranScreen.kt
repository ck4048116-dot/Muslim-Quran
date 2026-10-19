package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.QuranAudioPlayer
import com.example.data.local.AppDatabase
import com.example.data.local.QuranBookmark
import com.example.data.local.UserPreferences
import com.example.data.model.Ayah
import com.example.data.model.QuranRepository
import com.example.data.model.Surah
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    audioPlayer: QuranAudioPlayer,
    userPreferences: UserPreferences,
    database: AppDatabase,
    initialSurahNumber: Int? = null,
    onClearInitialSurah: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val playerState by audioPlayer.playerState.collectAsState()
    val bookmarks by database.quranBookmarkDao().getAllBookmarks().collectAsState(initial = emptyList())
    val arabicFontSize by userPreferences.arabicFontSize.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Meccan, 2: Medinan, 3: Bookmarks
    var activeSurah by remember { mutableStateOf<Surah?>(null) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialSurahNumber) {
        if (initialSurahNumber != null) {
            val target = QuranRepository.getSurah(initialSurahNumber)
            if (target != null) {
                activeSurah = target
                onClearInitialSurah?.invoke()
            }
        }
    }

    // BackHandler so Android back gesture returns from reader to Surah list
    if (activeSurah != null) {
        BackHandler {
            activeSurah = null
        }
    }

    if (activeSurah != null) {
        // Surah Reader View
        val surah = activeSurah!!
        val isBookmarked = bookmarks.any { it.surahNumber == surah.number }

        Column(
            modifier = modifier
                .fillMaxSize()
                .testTag("quran_reader_view")
        ) {
            // Reader Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { activeSurah = null },
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Surahs")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${surah.number}. ${surah.nameEnglish}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${surah.nameArabic} • ${surah.revelationType} • ${surah.numberOfAyahs} Verses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showFontSizeDialog = true },
                            modifier = Modifier.testTag("font_size_button")
                        ) {
                            Icon(Icons.Default.FormatSize, contentDescription = "Adjust font size")
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (isBookmarked) {
                                        database.quranBookmarkDao().removeBookmark(surah.number)
                                    } else {
                                        database.quranBookmarkDao().setBookmark(
                                            QuranBookmark(
                                                surahNumber = surah.number,
                                                surahName = surah.nameEnglish,
                                                lastAyahNumber = 1
                                            )
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.testTag("bookmark_surah_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Audio Reciter Floating Controller Bar
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Recitation • Sheikh Mishary Alafasy",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (playerState.currentAyah != null)
                                        "Ayah ${playerState.currentAyah?.ayahNumber} of ${surah.numberOfAyahs}"
                                    else
                                        "Tap Play or any Ayah to listen",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Play / Pause and Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { audioPlayer.previousAyah() },
                                enabled = playerState.currentAyahIndex > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Ayah")
                            }

                            FilledIconButton(
                                onClick = {
                                    if (playerState.currentSurah?.number == surah.number && (playerState.isPlaying || playerState.isLoading)) {
                                        audioPlayer.playPause()
                                    } else {
                                        audioPlayer.playAyah(surah, 0)
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("audio_play_pause_button")
                            ) {
                                if (playerState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause Recitation"
                                    )
                                }
                            }

                            IconButton(
                                onClick = { audioPlayer.nextAyah() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next Ayah")
                            }
                        }
                    }

                    if (playerState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playerState.errorMessage ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Surah Content List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bismillah Header
                if (surah.number != 9) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Ayahs
                val ayahs = surah.sampleAyahs
                if (ayahs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Surah ${surah.nameEnglish} (${surah.nameArabic})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This Surah contains ${surah.numberOfAyahs} verses. Audio recitation is ready to stream.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(ayahs.indices.toList()) { index ->
                        val ayah = ayahs[index]
                        val isPlayingThisAyah = playerState.isPlaying &&
                                playerState.currentSurah?.number == surah.number &&
                                playerState.currentAyah?.ayahNumber == ayah.ayahNumber

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ayah_card_${ayah.ayahNumber}")
                                .border(
                                    width = if (isPlayingThisAyah) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    audioPlayer.playAyah(surah, index)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPlayingThisAyah)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(if (isPlayingThisAyah) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Top row: Ayah badge and audio trigger
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isPlayingThisAyah) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPlayingThisAyah) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { audioPlayer.playAyah(surah, index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThisAyah) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                            contentDescription = "Recite Ayah ${ayah.ayahNumber}",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Arabic Ayah Text
                                Text(
                                    text = ayah.arabicText,
                                    fontSize = arabicFontSize.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    lineHeight = (arabicFontSize * 1.6f).sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Phonetic Transliteration
                                Text(
                                    text = ayah.transliteration,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // English Translation
                                Text(
                                    text = ayah.translation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Surah Directory & Search View
        val filteredSurahs = remember(searchQuery, selectedFilterTab, bookmarks) {
            QuranRepository.ALL_SURAHS.filter { surah ->
                val matchesSearch = surah.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                        surah.englishMeaning.contains(searchQuery, ignoreCase = true) ||
                        surah.number.toString() == searchQuery

                val matchesTab = when (selectedFilterTab) {
                    1 -> surah.revelationType == "Meccan"
                    2 -> surah.revelationType == "Medinan"
                    3 -> bookmarks.any { it.surahNumber == surah.number }
                    else -> true
                }
                matchesSearch && matchesTab
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .testTag("quran_directory_screen")
        ) {
            // Search Bar & Filter
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quran_search_field"),
                        placeholder = { Text("Search Surah by name or number...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Tabs
                    val tabs = listOf("All Surahs", "Meccan", "Medinan", "Bookmarked (${bookmarks.size})")
                    ScrollableTabRow(
                        selectedTabIndex = selectedFilterTab,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth(),
                        divider = {}
                    ) {
                        tabs.forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedFilterTab == idx,
                                onClick = { selectedFilterTab = idx },
                                text = { Text(title, fontWeight = if (selectedFilterTab == idx) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.testTag("tab_quran_$idx")
                            )
                        }
                    }
                }
            }

            // Surahs List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSurahs) { surah ->
                    val isBookmarked = bookmarks.any { it.surahNumber == surah.number }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { activeSurah = surah }
                            .testTag("surah_card_${surah.number}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Surah Number Diamond/Badge
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${surah.number}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = surah.nameEnglish,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isBookmarked) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = "Bookmarked",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${surah.englishMeaning} • ${surah.numberOfAyahs} verses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Arabic Name Calligraphy
                            Text(
                                text = surah.nameArabic,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Font size adjustment dialog
    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            title = { Text("Arabic Text Size", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = arabicFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                    Slider(
                        value = arabicFontSize,
                        onValueChange = { userPreferences.setArabicFontSize(it) },
                        valueRange = 20f..38f,
                        steps = 8
                    )
                    Text(
                        text = "${arabicFontSize.toInt()} sp",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontSizeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
