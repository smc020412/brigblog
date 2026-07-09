package com.smc020412.brigblog.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val SampleRate = 22_050
private const val MaxAmplitude = 32_767
private const val LowPriorityCooldownMs = 28L
private const val MasterVolumeBoost = 3.2f
private const val MenuMusicGain = 0.14f

class GameAudioManager(
    @Suppress("UNUSED_PARAMETER") context: Context
) {
    @Volatile
    private var volume = 0.75f
    @Volatile
    private var menuMusicRunning = false
    @Volatile
    private var currentMenuTrack: AudioTrack? = null
    private var lastEventAt = 0L
    private var menuMusicThread: Thread? = null

    fun setVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
    }

    @Synchronized
    fun play(event: GameSoundEvent) {
        val now = System.currentTimeMillis()
        if (event.priority <= GameSoundEvent.Move.priority && now - lastEventAt < LowPriorityCooldownMs) return

        lastEventAt = now
        val currentVolume = volume
        if (currentVolume <= 0.01f) return

        thread(name = "brigblog-sfx-${event.name}", isDaemon = true) {
            runCatching {
                playPcm(buildSound(event, currentVolume))
            }
        }
    }

    @Synchronized
    fun startMenuMusic() {
        if (menuMusicRunning) return

        menuMusicRunning = true
        menuMusicThread = thread(name = "brigblog-menu-bgm", isDaemon = true) {
            runCatching {
                playMenuMusicLoop()
            }.onFailure {
                menuMusicRunning = false
            }
        }
    }

    @Synchronized
    fun stopMenuMusic() {
        menuMusicRunning = false
        currentMenuTrack?.pause()
        currentMenuTrack?.flush()
        currentMenuTrack?.release()
        currentMenuTrack = null
        menuMusicThread?.interrupt()
        menuMusicThread = null
    }

    fun release() {
        stopMenuMusic()
    }

    private fun playPcm(samples: ShortArray) {
        if (samples.isEmpty()) return

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * Short.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep(samples.size * 1000L / SampleRate + 24L)
        } finally {
            track.release()
        }
    }

    private fun playMenuMusicLoop() {
        val menuBar = buildMenuMusicBar(volume)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(menuBar.size * Short.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            currentMenuTrack = track
            track.play()
            while (menuMusicRunning) {
                val bar = buildMenuMusicBar(volume)
                var offset = 0
                while (menuMusicRunning && offset < bar.size) {
                    val chunkSize = minOf(MenuStepSampleCount, bar.size - offset)
                    track.write(bar, offset, chunkSize)
                    offset += chunkSize
                }
            }
        } finally {
            if (currentMenuTrack === track) currentMenuTrack = null
            runCatching { track.release() }
        }
    }

    private fun buildSound(event: GameSoundEvent, volume: Float): ShortArray {
        val steps = when (event) {
            GameSoundEvent.Move -> listOf(
                SoundStep(420f, 520f, 28, Wave.Square, 0.34f)
            )
            GameSoundEvent.Rotate -> listOf(
                SoundStep(620f, 720f, 26, Wave.Square, 0.38f),
                SoundStep(860f, 820f, 22, Wave.Square, 0.28f)
            )
            GameSoundEvent.SoftDrop -> listOf(
                SoundStep(260f, 210f, 34, Wave.Triangle, 0.30f)
            )
            GameSoundEvent.HardDrop -> listOf(
                SoundStep(180f, 95f, 54, Wave.Square, 0.70f),
                SoundStep(70f, 45f, 42, Wave.Noise, 0.46f)
            )
            GameSoundEvent.Hold -> listOf(
                SoundStep(360f, 360f, 34, Wave.Square, 0.36f),
                SoundStep(540f, 620f, 46, Wave.Square, 0.44f)
            )
            GameSoundEvent.Start -> listOf(
                SoundStep(520f, 660f, 48, Wave.Square, 0.46f),
                SoundStep(660f, 880f, 48, Wave.Square, 0.52f),
                SoundStep(880f, 1320f, 68, Wave.Square, 0.58f),
                SoundStep(1320f, 1760f, 74, Wave.Triangle, 0.42f)
            )
            GameSoundEvent.LineClear -> listOf(
                SoundStep(520f, 760f, 42, Wave.Square, 0.48f),
                SoundStep(760f, 1040f, 54, Wave.Square, 0.58f),
                SoundStep(1200f, 900f, 42, Wave.Noise, 0.26f)
            )
            GameSoundEvent.GameOver -> listOf(
                SoundStep(290f, 230f, 72, Wave.Square, 0.60f),
                SoundStep(220f, 160f, 92, Wave.Triangle, 0.70f),
                SoundStep(145f, 92f, 128, Wave.Square, 0.76f),
                SoundStep(88f, 55f, 150, Wave.Noise, 0.26f)
            )
            GameSoundEvent.Menu -> listOf(
                SoundStep(700f, 560f, 34, Wave.Square, 0.30f)
            )
        }

        return steps.flatMapToSamples(masterVolume = volume)
    }
}

private enum class Wave {
    Square,
    Triangle,
    Noise
}

private data class SoundStep(
    val startHz: Float,
    val endHz: Float,
    val durationMs: Int,
    val wave: Wave,
    val gain: Float
)

private fun List<SoundStep>.flatMapToSamples(masterVolume: Float): ShortArray {
    val samples = ArrayList<Short>(sumOf { it.durationMs * SampleRate / 1000 })

    for (step in this) {
        val sampleCount = (step.durationMs * SampleRate / 1000f).roundToInt().coerceAtLeast(1)
        var phase = 0.0

        repeat(sampleCount) { index ->
            val progress = index.toFloat() / sampleCount
            val hz = step.startHz + (step.endHz - step.startHz) * progress
            phase += hz / SampleRate

            val raw = when (step.wave) {
                Wave.Square -> if (phase % 1.0 < 0.5) 1.0 else -1.0
                Wave.Triangle -> 2.0 * kotlin.math.abs(2.0 * (phase % 1.0) - 1.0) - 1.0
                Wave.Noise -> Random.nextDouble(-1.0, 1.0)
            }
            val envelope = envelope(index, sampleCount)
            val boosted = (raw * envelope * step.gain * masterVolume * MasterVolumeBoost).coerceIn(-1.0, 1.0)
            val amp = boosted * MaxAmplitude
            samples.add(amp.roundToInt().coerceIn(-MaxAmplitude, MaxAmplitude).toShort())
        }
    }

    return samples.toShortArray()
}

private fun envelope(index: Int, sampleCount: Int): Double {
    val progress = index.toDouble() / sampleCount
    val attack = (progress / 0.08).coerceIn(0.0, 1.0)
    val release = ((1.0 - progress) / 0.22).coerceIn(0.0, 1.0)
    val clickSoftener = sin(progress * PI).coerceAtLeast(0.0)
    return attack * release * (0.72 + clickSoftener * 0.28)
}

private const val MenuStepMs = 150
private const val MenuBarSteps = 16
private const val MenuStepSampleCount = SampleRate * MenuStepMs / 1000
private const val MenuBarSampleCount = SampleRate * MenuStepMs * MenuBarSteps / 1000

private val MenuLeadNotes = floatArrayOf(
    392f, 493.88f, 587.33f, 493.88f,
    659.25f, 587.33f, 493.88f, 392f,
    440f, 554.37f, 659.25f, 554.37f,
    739.99f, 659.25f, 554.37f, 440f
)

private val MenuBassNotes = floatArrayOf(
    98f, 98f, 123.47f, 123.47f,
    146.83f, 146.83f, 123.47f, 123.47f,
    110f, 110f, 130.81f, 130.81f,
    164.81f, 164.81f, 130.81f, 130.81f
)

private fun buildMenuMusicBar(masterVolume: Float): ShortArray {
    val samples = ShortArray(MenuBarSampleCount)
    val stepSamples = MenuStepSampleCount
    val gain = masterVolume * MasterVolumeBoost * MenuMusicGain

    for (i in samples.indices) {
        val step = (i / stepSamples).coerceIn(0, MenuBarSteps - 1)
        val inStep = i % stepSamples
        val stepProgress = inStep.toDouble() / stepSamples
        val lead = pulseWave(MenuLeadNotes[step], i, duty = 0.38)
        val bass = pulseWave(MenuBassNotes[step], i, duty = 0.50)
        val arpeggioHz = MenuLeadNotes[(step + 4) % MenuLeadNotes.size] * if (step % 4 == 0) 1.5f else 1.0f
        val sparkle = pulseWave(arpeggioHz, i, duty = 0.22) * if (step % 2 == 0) 0.20 else 0.0
        val noteEnvelope = if (stepProgress < 0.82) 1.0 else ((1.0 - stepProgress) / 0.18).coerceIn(0.0, 1.0)
        val raw = (lead * 0.46 + bass * 0.30 + sparkle) * noteEnvelope
        val sample = (raw * gain).coerceIn(-1.0, 1.0) * MaxAmplitude
        samples[i] = sample.roundToInt().coerceIn(-MaxAmplitude, MaxAmplitude).toShort()
    }

    return samples
}

private fun pulseWave(hz: Float, sampleIndex: Int, duty: Double): Double {
    val phase = (sampleIndex * hz / SampleRate) % 1.0
    val square = if (phase < duty) 1.0 else -1.0
    return square * (0.84 + abs(phase - 0.5) * 0.16)
}

enum class GameSoundEvent(val priority: Int) {
    Move(1),
    Rotate(1),
    SoftDrop(1),
    HardDrop(2),
    Hold(2),
    Start(4),
    LineClear(3),
    GameOver(4),
    Menu(2)
}
