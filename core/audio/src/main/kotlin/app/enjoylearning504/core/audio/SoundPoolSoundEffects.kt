package app.enjoylearning504.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import app.enjoylearning504.core.domain.platform.SoundEffect
import app.enjoylearning504.core.domain.platform.SoundEffects

/**
 * Plays the bundled feedback sounds (made by `tools/generate-sounds.py`) through one [SoundPool].
 * The samples load on the first [prepare] or [play], off the startup path, and a sound that
 * hasn't finished loading is skipped rather than waited for.
 */
class SoundPoolSoundEffects(context: Context) : SoundEffects {
    private val appContext = context.applicationContext
    private val samples by lazy { Samples() }

    override fun prepare() {
        samples
    }

    override fun play(effect: SoundEffect) = samples.play(effect)

    private inner class Samples {
        private val loaded = mutableSetOf<Int>()

        private val pool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
            .apply {
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) synchronized(loaded) { loaded += sampleId }
                }
            }

        private val ids = SoundEffect.entries.associateWith { pool.load(appContext, it.resource(), 1) }

        fun play(effect: SoundEffect) {
            val id = ids.getValue(effect)
            if (synchronized(loaded) { id in loaded }) pool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun SoundEffect.resource() = when (this) {
        SoundEffect.Correct -> R.raw.sfx_correct
        SoundEffect.Wrong -> R.raw.sfx_wrong
        SoundEffect.Complete -> R.raw.sfx_complete
    }
}
