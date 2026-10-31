package app.enjoylearning504.core.ads

import org.koin.dsl.module

val adsModule = module {
    single<AdsController> { TapsellAdsController() }
}
