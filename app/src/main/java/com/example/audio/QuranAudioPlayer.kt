package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.data.model.Ayah
import com.example.data.model.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuranPlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurah: Surah? = null,
    val currentAyah: Ayah? = null,
    val currentAyahIndex: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isLoopingSingleAyah: Boolean = false,
    val errorMessage: String? = null
)

class QuranAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    private val _playerState = MutableStateFlow(QuranPlayerState())
    val playerState: StateFlow<QuranPlayerState> = _playerState.asStateFlow()

    private var currentSurahAyahs: List<Ayah> = emptyList()

    fun playAyah(surah: Surah, ayahIndex: Int) {
        val ayahs = surah.sampleAyahs
        if (ayahs.isEmpty()) {
            _playerState.value = _playerState.value.copy(
                errorMessage = "Audio verses for ${surah.nameEnglish} are preparing."
            )
            return
        }

        val clampedIndex = ayahIndex.coerceIn(0, ayahs.lastIndex)
        val targetAyah = ayahs[clampedIndex]
        currentSurahAyahs = ayahs

        stop()

        _playerState.value = _playerState.value.copy(
            isLoading = true,
            currentSurah = surah,
            currentAyah = targetAyah,
            currentAyahIndex = clampedIndex,
            errorMessage = null
        )

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(targetAyah.audioUrl)
                setOnPreparedListener { mp ->
                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        isPlaying = true
                    )
                    applyPlaybackSpeed(mp, _playerState.value.playbackSpeed)
                    mp.start()
                }
                setOnCompletionListener {
                    handleAyahCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("QuranAudioPlayer", "Playback error what=$what extra=$extra")
                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        isPlaying = false,
                        errorMessage = "Unable to stream audio. Please check network."
                    )
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Setup error", e)
            _playerState.value = _playerState.value.copy(
                isLoading = false,
                isPlaying = false,
                errorMessage = e.localizedMessage
            )
        }
    }

    fun playPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
        } else {
            player.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
        }
    }

    fun nextAyah() {
        val current = _playerState.value
        val surah = current.currentSurah ?: return
        if (currentSurahAyahs.isNotEmpty() && current.currentAyahIndex < currentSurahAyahs.lastIndex) {
            playAyah(surah, current.currentAyahIndex + 1)
        }
    }

    fun previousAyah() {
        val current = _playerState.value
        val surah = current.currentSurah ?: return
        if (currentSurahAyahs.isNotEmpty() && current.currentAyahIndex > 0) {
            playAyah(surah, current.currentAyahIndex - 1)
        }
    }

    fun toggleLoopSingleAyah() {
        val current = _playerState.value
        _playerState.value = current.copy(isLoopingSingleAyah = !current.isLoopingSingleAyah)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        mediaPlayer?.let { mp ->
            applyPlaybackSpeed(mp, speed)
        }
    }

    private fun applyPlaybackSpeed(mp: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mp.playbackParams = mp.playbackParams.setSpeed(speed)
            } catch (e: Exception) {
                Log.w("QuranAudioPlayer", "Could not set playback speed", e)
            }
        }
    }

    private fun handleAyahCompleted() {
        val state = _playerState.value
        val surah = state.currentSurah ?: return

        if (state.isLoopingSingleAyah) {
            playAyah(surah, state.currentAyahIndex)
            return
        }

        if (state.currentAyahIndex < currentSurahAyahs.lastIndex) {
            // Auto advance to next ayah
            playAyah(surah, state.currentAyahIndex + 1)
        } else {
            _playerState.value = state.copy(isPlaying = false)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        _playerState.value = _playerState.value.copy(isPlaying = false, isLoading = false)
    }

    fun release() {
        stop()
    }
}
