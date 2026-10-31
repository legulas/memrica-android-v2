package app.enjoylearning504.core.network

import org.koin.core.qualifier.named
import org.koin.dsl.module

private val ConfigClient = named("configClient")
private val MemricaClient = named("memricaClient")

/** Expects a [TokenProvider] binding from the data layer. */
val networkModule = module {
    single(ConfigClient) { HttpClientFactory.configClient() }
    single(MemricaClient) { HttpClientFactory.memricaClient(get()) }
    single { ConfigApi(get(ConfigClient)) }
    single { AuthApi(get(MemricaClient)) }
}
