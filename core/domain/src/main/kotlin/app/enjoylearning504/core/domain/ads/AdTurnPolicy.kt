package app.enjoylearning504.core.domain.ads

/**
 * Decides whether a screen that can end with an interstitial ad should show one.
 *
 * Mirrors the original counter: with a turn of N, ads come on the 2nd screen, then every N
 * screens after that (2, 2+N, 2+2N, ...). A turn of 0 shows an ad every time.
 * The counter lives for the process, like the original static field.
 */
class AdTurnPolicy {
    private var counter = 0

    @Synchronized
    fun nextTurnShowsAd(turn: Int): Boolean {
        if (turn == 0) return true
        if (counter == turn) counter = 0
        return if (counter == 1) {
            counter++
            true
        } else {
            if (counter < turn) counter++
            false
        }
    }

    companion object {
        /** Used when the remote config can't be read, or holds an out-of-range value. */
        const val DEFAULT_TURN = 3
        private const val MAX_TURN_EXCLUSIVE = 6

        fun sanitize(remoteValue: Int?): Int =
            if (remoteValue != null && remoteValue in 0 until MAX_TURN_EXCLUSIVE) remoteValue else DEFAULT_TURN
    }
}
