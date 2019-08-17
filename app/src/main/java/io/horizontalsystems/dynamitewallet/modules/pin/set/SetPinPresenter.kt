package io.horizontalsystems.dynamitewallet.modules.pin.set

import io.horizontalsystems.dynamitewallet.R
import io.horizontalsystems.dynamitewallet.modules.pin.ManagePinPresenter
import io.horizontalsystems.dynamitewallet.modules.pin.PinModule
import io.horizontalsystems.dynamitewallet.modules.pin.PinPage

class SetPinPresenter(
        private val interactor: PinModule.IPinInteractor,
        private val router: SetPinModule.ISetPinRouter): ManagePinPresenter(interactor, pages = listOf(Page.ENTER, Page.CONFIRM)) {

    override fun viewDidLoad() {
        view?.setTitle(R.string.SetPin_Title)

        val pinPages = mutableListOf<PinPage>()
        pages.forEach { page ->
            when(page) {
                Page.ENTER -> pinPages.add(PinPage(R.string.SetPin_Info))
                Page.CONFIRM -> pinPages.add(PinPage(R.string.SetPin_ConfirmInfo))
            }
        }
        view?.addPages(pinPages)
    }

    override fun didSavePin() {
        interactor.startAdapters()
    }

    override fun didStartedAdapters() {
        router.navigateToMain()
    }
}
