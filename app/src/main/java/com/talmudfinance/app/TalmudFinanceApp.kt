package com.talmudfinance.app

import android.app.Application
import com.talmudfinance.app.data.repository.MarketRepository
import com.talmudfinance.app.data.repository.TalmudRepository

/**
 * シンプルなサービスロケータを兼ねる Application クラス。
 * MVPなのでDIフレームワーク（Hilt等）は導入していない。
 */
class TalmudFinanceApp : Application() {

    lateinit var marketRepository: MarketRepository
        private set
    lateinit var talmudRepository: TalmudRepository
        private set

    override fun onCreate() {
        super.onCreate()
        marketRepository = MarketRepository()
        talmudRepository = TalmudRepository(applicationContext)
    }
}
