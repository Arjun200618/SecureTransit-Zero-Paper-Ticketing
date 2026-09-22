package com.example.rtcpos.soundbox

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

/**
 * Handheld POS Soundbox Engine.
 *
 * Simulates dedicated IoT transit audio soundbox hardware:
 * 1. Generates authentic two-tone acoustic chime ("Ding-Dong" 880Hz -> 1320Hz)
 * 2. Employs Text-To-Speech for high-volume multilingual payment receipt voice announcements.
 * 3. Triggers haptic vibration pulse for conductor confirmation in loud bus environments.
 */
class SoundboxManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val scope = CoroutineScope(Dispatchers.Default)

    enum class SoundboxLanguage(val displayName: String, val locale: Locale) {
        ENGLISH("English", Locale("en", "IN")),
        TELUGU("తెలుగు (Telugu)", Locale("te", "IN")),
        HINDI("हिन्दी (Hindi)", Locale("hi", "IN"))
    }

    var selectedLanguage: SoundboxLanguage = SoundboxLanguage.ENGLISH

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setSpeechRate(1.05f)
            tts?.setPitch(1.0f)
            applyLocale(selectedLanguage.locale)
        } else {
            Log.w("SoundboxManager", "TTS initialization failed with status: $status")
        }
    }

    private fun applyLocale(locale: Locale) {
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English (India)
            tts?.setLanguage(Locale("en", "IN"))
        }
    }

    /**
     * Announces UPI payment receipt: Chime -> Haptic -> Voice Announcement.
     */
    fun announcePayment(amount: Double, language: SoundboxLanguage = selectedLanguage) {
        selectedLanguage = language
        scope.launch {
            // 1. Trigger dual-tone acoustic chime
            playPaymentChime()

            // 2. Trigger tactile confirmation pulse
            triggerHapticPulse()

            delay(350) // small breath between chime and voice

            // 3. Multilingual voice synthesis
            val message = when (language) {
                SoundboxLanguage.ENGLISH -> "Payment of ${amount.toInt()} Rupees received via UPI"
                SoundboxLanguage.TELUGU -> "యూపీఐ ద్వారా ${amount.toInt()} రూపాయలు చెల్లించబడింది"
                SoundboxLanguage.HINDI -> "यू पी आई से ${amount.toInt()} रुपये प्राप्त हुए"
            }

            tts?.let { engine ->
                if (isTtsReady) {
                    applyLocale(language.locale)
                    engine.speak(message, TextToSpeech.QUEUE_FLUSH, null, "RTC_UPI_${System.currentTimeMillis()}")
                }
            }
        }
    }

    /**
     * Synthesizes audio PCM sine wave tones for instant audio playback without bundling heavy audio files.
     */
    private fun playPaymentChime() {
        try {
            val sampleRate = 44100
            val durationTone1 = 0.14 // 140ms at 880Hz (A5)
            val durationTone2 = 0.22 // 220ms at 1320Hz (E6)

            val numSamples1 = (durationTone1 * sampleRate).toInt()
            val numSamples2 = (durationTone2 * sampleRate).toInt()

            val buffer = ShortArray(numSamples1 + numSamples2)

            // Tone 1: 880 Hz
            for (i in 0 until numSamples1) {
                val time = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * 880.0 * time
                val envelope = 1.0 - (i.toDouble() / numSamples1 * 0.2) // slight fade
                buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
            }

            // Tone 2: 1320 Hz
            for (i in 0 until numSamples2) {
                val time = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * 1320.0 * time
                val envelope = (1.0 - (i.toDouble() / numSamples2)).coerceIn(0.0, 1.0) // decay
                buffer[numSamples1 + i] = (sin(angle) * Short.MAX_VALUE * 0.85 * envelope).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
        } catch (e: Exception) {
            Log.e("SoundboxManager", "Error playing chime tone", e)
        }
    }

    private fun triggerHapticPulse() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 180), intArrayOf(0, 200, 0, 255), -1)
                vm?.vibrate(CombinedVibration.createParallel(effect))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 180), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 120, 80, 180), -1)
                }
            }
        } catch (e: Exception) {
            Log.w("SoundboxManager", "Vibrator unavailable", e)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
