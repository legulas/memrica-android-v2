package app.enjoylearning504

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

class KoinGraphTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `every dependency in the graph can be resolved`() {
        module { includes(allModules) }.verify(
            // Built from the Android context or by factories, not resolved through Koin
            extraTypes = listOf(
                Context::class,
                SavedStateHandle::class,
                DataStore::class,
                HttpClient::class,
                HttpClientEngine::class,
                CoroutineDispatcher::class,
                Function0::class,
            ),
        )
    }
}
