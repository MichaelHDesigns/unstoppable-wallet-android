package io.horizontalsystems.bankwallet.modules.fulltransactioninfo

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.horizontalsystems.bankwallet.entities.FullTransactionItem
import io.horizontalsystems.bankwallet.entities.FullTransactionRecord
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class FullTransactionInfoPresenterTest {

    private val interactor = mock(FullTransactionInfoInteractor::class.java)
    private val state = mock(FullTransactionInfoState::class.java)
    private val router = mock(FullTransactionInfoModule.Router::class.java)
    private val view = mock(FullTransactionInfoModule.View::class.java)
    private val transactionItem = mock(FullTransactionItem::class.java)
    private val transactionRecord = mock(FullTransactionRecord::class.java)
    private val transactionHash = "abc"
    private val transactionUrl = "http://domain.com"

    private lateinit var presenter: FullTransactionInfoPresenter

    @Before
    fun setup() {
        whenever(state.transactionHash).thenReturn(transactionHash)
        whenever(interactor.url(transactionHash)).thenReturn(transactionUrl)

        presenter = FullTransactionInfoPresenter(interactor, router, state)
        presenter.view = view
    }

    @Test
    fun viewDidLoad() {
        presenter.viewDidLoad()

        verify(view).showLoading()
        verify(interactor).retrieveTransactionInfo(transactionHash)
    }

    @Test
    fun onReceiveTransactionInfo() {
        presenter.onReceiveTransactionInfo(transactionRecord)

        verify(state).transactionRecord = transactionRecord
        verify(view).hideLoading()
        verify(view).reload()
    }

    @Test
    fun onTapItem_copy() {
        whenever(transactionItem.clickable).thenReturn(true)
        whenever(transactionItem.value).thenReturn(transactionHash)

        presenter.onTapItem(transactionItem)

        verify(interactor).copyToClipboard(transactionItem.value!!)
    }

    @Test
    fun onTapItem_openUrl() {
        whenever(transactionItem.clickable).thenReturn(true)
        whenever(transactionItem.url).thenReturn(transactionUrl)

        presenter.onTapItem(transactionItem)

        verify(view).openUrl(transactionUrl)
    }

    @Test
    fun onTapResource() {
        presenter.onTapResource()

        verify(view).openUrl(transactionUrl)
    }

    @Test
    fun onShare() {
        presenter.onShare()

        verify(view).share(transactionUrl)
    }

    @Test
    fun onError() {
        presenter.onError("abc")

        verify(view).hideLoading()
        verify(view).showError("abc")
    }

    @Test
    fun retryLoadInfo() {
        whenever(state.transactionRecord).thenReturn(null)
        presenter.retryLoadInfo()

        verify(view).hideError()
        verify(view).showLoading()
        verify(interactor).retrieveTransactionInfo(transactionHash)
    }

}
