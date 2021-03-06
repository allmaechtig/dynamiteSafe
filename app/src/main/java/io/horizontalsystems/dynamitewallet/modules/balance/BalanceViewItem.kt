package io.horizontalsystems.dynamitewallet.modules.balance

import io.horizontalsystems.dynamitewallet.core.AdapterState
import io.horizontalsystems.dynamitewallet.entities.Coin
import io.horizontalsystems.dynamitewallet.entities.CoinValue
import io.horizontalsystems.dynamitewallet.entities.Currency
import io.horizontalsystems.dynamitewallet.entities.CurrencyValue
import java.math.BigDecimal

data class BalanceViewItem(
        val coin: Coin,
        val coinValue: CoinValue,
        val exchangeValue: CurrencyValue?,
        val currencyValue: CurrencyValue?,
        val state: AdapterState,
        val rateExpired: Boolean
)

data class BalanceHeaderViewItem(
        val currencyValue: CurrencyValue?,
        val upToDate: Boolean
)

class BalanceViewItemFactory {

    fun createViewItem(item: BalanceModule.BalanceItem, currency: Currency?): BalanceViewItem {
        var exchangeValue: CurrencyValue? = null
        var currencyValue: CurrencyValue? = null

        item.rate?.let { rate ->
            currency?.let {
                exchangeValue = CurrencyValue(it, rate.value)
                currencyValue = CurrencyValue(it, rate.value * item.balance)
            }
        }

        return BalanceViewItem(
                item.coin,
                CoinValue(item.coin.code, item.balance),
                exchangeValue,
                currencyValue,
                item.state,
                item.rate?.expired ?: false
        )
    }

    fun createHeaderViewItem(items: List<BalanceModule.BalanceItem>, currency: Currency?): BalanceHeaderViewItem {
        var sum = BigDecimal.ZERO
        var expired = false
        val nonZeroItems = items.filter { it.balance > BigDecimal.ZERO }

        nonZeroItems.forEach { balanceItem ->
            val rate = balanceItem.rate

            rate?.value?.times(balanceItem.balance)?.let {
                sum += it
            }

            expired = expired || balanceItem.state != AdapterState.Synced || rate == null || rate.expired
        }

        return BalanceHeaderViewItem(currency?.let { CurrencyValue(it, sum) }, !expired)
    }

}
