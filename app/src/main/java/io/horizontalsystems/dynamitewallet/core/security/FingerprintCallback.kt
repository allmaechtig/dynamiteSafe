package io.horizontalsystems.dynamitewallet.core.security

interface FingerprintCallback {
    fun onAuthenticated()

    fun onAuthenticationHelp(helpString: CharSequence?)

    fun onAuthenticationFailed()

    fun onAuthenticationError(errMsgId: Int, errString: CharSequence?)
}
