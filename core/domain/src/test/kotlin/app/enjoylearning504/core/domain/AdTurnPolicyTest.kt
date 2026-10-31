package app.enjoylearning504.core.domain

import app.enjoylearning504.core.domain.ads.AdTurnPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class AdTurnPolicyTest {
    @Test
    fun `turn of three shows ads on screens 2, 5 and 8 like the original`() {
        val policy = AdTurnPolicy()
        val shown = (1..9).filter { policy.nextTurnShowsAd(3) }
        assertEquals(listOf(2, 5, 8), shown)
    }

    @Test
    fun `turn of zero always shows an ad`() {
        val policy = AdTurnPolicy()
        assertEquals(List(4) { true }, List(4) { policy.nextTurnShowsAd(0) })
    }

    @Test
    fun `remote values of six or more fall back to three`() {
        assertEquals(3, AdTurnPolicy.sanitize(6))
        assertEquals(3, AdTurnPolicy.sanitize(null))
        assertEquals(5, AdTurnPolicy.sanitize(5))
        assertEquals(0, AdTurnPolicy.sanitize(0))
    }
}
