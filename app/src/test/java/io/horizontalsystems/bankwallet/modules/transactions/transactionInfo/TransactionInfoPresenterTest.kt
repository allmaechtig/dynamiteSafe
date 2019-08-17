package io.horizontalsystems.dynamitewallet.modules.transactions.transactionInfo

import com.nhaarman.mockito_kotlin.whenever
import io.horizontalsystems.dynamitewallet.entities.Coin
import io.horizontalsystems.dynamitewallet.entities.CoinType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify

class TransactionInfoPresenterTest {
    private val interactor = Mockito.mock(TransactionInfoModule.Interactor::class.java)
    private val router = Mockito.mock(TransactionInfoModule.Router::class.java)
    private val view = Mockito.mock(TransactionInfoModule.View::class.java)
    private val coin = Mockito.mock(Coin::class.java)

    private lateinit var presenter: TransactionInfoPresenter

    @Before
    fun setUp() {
        whenever(coin.type).thenReturn(Mockito.mock(CoinType.Bitcoin::class.java))
        presenter = TransactionInfoPresenter(interactor, router)
        presenter.view = view
    }


    @Test
    fun onCopy() {
        val value = "some string"

        presenter.onCopy(value)

        verify(interactor).onCopy(value)
        verify(view).showCopied()
    }


    @Test
    fun openFullInfo() {
        val transactionHash = "hash"

        presenter.openFullInfo(transactionHash, coin)
        verify(router).openFullInfo(transactionHash, coin)
    }

}
