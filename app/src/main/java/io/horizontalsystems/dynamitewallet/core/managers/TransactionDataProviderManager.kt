package io.horizontalsystems.dynamitewallet.core.managers

import io.horizontalsystems.dynamitewallet.core.IAppConfigProvider
import io.horizontalsystems.dynamitewallet.core.ILocalStorage
import io.horizontalsystems.dynamitewallet.core.ITransactionDataProviderManager
import io.horizontalsystems.dynamitewallet.entities.Coin
import io.horizontalsystems.dynamitewallet.entities.CoinType
import io.horizontalsystems.dynamitewallet.modules.fulltransactioninfo.FullTransactionInfoModule.BitcoinForksProvider
import io.horizontalsystems.dynamitewallet.modules.fulltransactioninfo.FullTransactionInfoModule.EthereumForksProvider
import io.horizontalsystems.dynamitewallet.modules.fulltransactioninfo.FullTransactionInfoModule.Provider
import io.horizontalsystems.dynamitewallet.modules.fulltransactioninfo.providers.*
import io.reactivex.subjects.PublishSubject

class TransactionDataProviderManager(appConfig: IAppConfigProvider, private val localStorage: ILocalStorage)
    : ITransactionDataProviderManager {

    private val bitcoinProviders = when {
        appConfig.testMode -> listOf(HorsysBitcoinProvider(testMode = true))
        else -> listOf(
                HorsysBitcoinProvider(testMode = false),
                BlockChairBitcoinProvider(),
                BtcComBitcoinProvider())
    }

    private val bitcoinCashProviders = when {
        appConfig.testMode -> listOf(BlockdozerBitcoinCashProvider(true))
        else -> listOf(
                BlockdozerBitcoinCashProvider(false),
                BlockChairBitcoinCashProvider(),
                BtcComBitcoinCashProvider())
    }

    private val ethereumProviders = when {
        appConfig.testMode -> listOf(
                EtherscanEthereumProvider(testMode = true))
        else -> listOf(
                EtherscanEthereumProvider(testMode = false),
                BlockChairEthereumProvider())
    }

    private val dashProviders = when {
        appConfig.testMode -> listOf(HorsysDashProvider(true))
        else -> listOf(
                HorsysDashProvider(false),
                BlockChairDashProvider(),
                InsightDashProvider()
        )
    }

    override val baseProviderUpdatedSignal = PublishSubject.create<Unit>()

    override fun providers(coin: Coin): List<Provider> = when (coin.type) {
        is CoinType.Bitcoin -> bitcoinProviders
        is CoinType.BitcoinCash -> bitcoinCashProviders
        is CoinType.Ethereum, is CoinType.Erc20 -> ethereumProviders
        is CoinType.Dash -> dashProviders
    }

    override fun baseProvider(coin: Coin) = when (coin.type) {
        is CoinType.Bitcoin, is CoinType.BitcoinCash -> {
            bitcoin(localStorage.baseBitcoinProvider ?: bitcoinProviders[0].name)
        }
        is CoinType.Ethereum, is CoinType.Erc20 -> {
            ethereum(localStorage.baseEthereumProvider ?: ethereumProviders[0].name)
        }
        is CoinType.Dash -> {
            dash(localStorage.baseDashProvider ?: dashProviders[0].name)
        }
    }

    override fun setBaseProvider(name: String, coin: Coin) {
        when (coin.type) {
            is CoinType.Bitcoin, is CoinType.BitcoinCash -> {
                localStorage.baseBitcoinProvider = name
            }
            is CoinType.Ethereum, is CoinType.Erc20 -> {
                localStorage.baseEthereumProvider = name
            }
            is CoinType.Dash -> {
                localStorage.baseDashProvider = name
            }
        }

        baseProviderUpdatedSignal.onNext(Unit)
    }

    //
    // Providers
    //
    override fun bitcoin(name: String): BitcoinForksProvider {
        bitcoinProviders.let { list ->
            return list.find { it.name == name } ?: list[0]
        }
    }

    override fun bitcoinCash(name: String): BitcoinForksProvider {
        bitcoinCashProviders.let { list ->
            return list.find { it.name == name } ?: list[0]
        }
    }

    override fun dash(name: String): BitcoinForksProvider {
        dashProviders.let { list ->
            return list.find { it.name == name } ?: list[0]
        }
    }

    override fun ethereum(name: String): EthereumForksProvider {
        ethereumProviders.let { list ->
            return list.find { it.name == name } ?: list[0]
        }
    }
}
