package app.enjoylearning504.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import app.enjoylearning504.core.domain.platform.PronunciationPlayer
import app.enjoylearning504.core.model.Word
import java.util.Locale

/**
 * Word recordings play through one shared Media3 ExoPlayer from `assets/audio`.
 * Example sentences have no recordings, so they are read by the system text-to-speech engine,
 * as in the original app. Starting either one stops whatever was playing.
 */
class ExoPronunciationPlayer(context: Context) : PronunciationPlayer {
    private val appContext = context.applicationContext

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(appContext).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true,
            )
        }
    }

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSentence: String? = null

    override fun playWord(word: Word) {
        val asset = word.audioAsset ?: return
        stopSpeech()
        player.setMediaItem(MediaItem.fromUri("asset:///$asset"))
        player.prepare()
        player.play()
    }

    override fun speakSentence(sentence: String) {
        player.stop()
        val engine = tts
        if (engine != null && ttsReady) {
            engine.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
            return
        }
        pendingSentence = sentence
        if (engine == null) {
            tts = TextToSpeech(appContext) { status ->
                ttsReady = status == TextToSpeech.SUCCESS
                if (ttsReady) {
                    tts?.language = Locale.US
                    pendingSentence?.let { tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID) }
                }
                pendingSentence = null
            }
        }
    }

    override fun stop() {
        player.stop()
        stopSpeech()
    }

    private fun stopSpeech() {
        tts?.stop()
    }

    private companion object {
        const val UTTERANCE_ID = "sentence"
    }
}
