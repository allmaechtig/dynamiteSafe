package io.horizontalsystems.dynamitewallet.core.managers

import io.horizontalsystems.dynamitewallet.core.App
import io.horizontalsystems.dynamitewallet.core.ILocalStorage
import io.horizontalsystems.dynamitewallet.entities.SyncMode
import io.horizontalsystems.dynamitewallet.modules.balance.BalanceSortType
import io.horizontalsystems.dynamitewallet.modules.send.SendModule


class LocalStorageManager : ILocalStorage {

    private val CURRENT_LANGUAGE = "current_language"
    private val LIGHT_MODE_ENABLED = "light_mode_enabled"
    private val FINGERPRINT_ENABLED = "fingerprint_enabled"
    private val SEND_INPUT_TYPE = "send_input_type"
    private val WORDLIST_BACKUP = "wordlist_backup"
    private val I_UNDERSTAND = "i_understand"
    private val BLOCK_TILL_DATE = "unblock_date"
    private val BASE_CURRENCY_CODE = "base_currency_code"
    private val FAILED_ATTEMPTS = "failed_attempts"
    private val LOCKOUT_TIMESTAMP = "lockout_timestamp"
    private val BASE_BITCOIN_PROVIDER = "base_bitcoin_provider"
    private val BASE_ETHEREUM_PROVIDER = "base_ethereum_provider"
    private val BASE_DASH_PROVIDER = "base_dash_provider"
    private val SYNC_MODE = "sync_mode"
    private val SORT_TYPE = "balance_sort_type"

    override var currentLanguage: String?
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(CURRENT_LANGUAGE, null)
        set(language) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putString(CURRENT_LANGUAGE, language).apply()
        }

    override var isBackedUp: Boolean
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getBoolean(WORDLIST_BACKUP, false)
        set(backedUp) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putBoolean(WORDLIST_BACKUP, backedUp).apply()
        }

    override var isBiometricOn: Boolean
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getBoolean(FINGERPRINT_ENABLED, false)
        set(enabled) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putBoolean(FINGERPRINT_ENABLED, enabled).apply()
        }

    override var sendInputType: SendModule.InputType?
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(SEND_INPUT_TYPE, null)?.let {
            try {
                SendModule.InputType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        set(value) {
            val editor = io.horizontalsystems.dynamitewallet.core.App.preferences.edit()
            when (value) {
                null -> editor.remove(SEND_INPUT_TYPE).apply()
                else -> editor.putString(SEND_INPUT_TYPE, value.name).apply()
            }
        }

    override var isLightModeOn: Boolean
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getBoolean(LIGHT_MODE_ENABLED, false)
        set(enabled) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putBoolean(LIGHT_MODE_ENABLED, enabled).apply()
        }

    override var iUnderstand: Boolean
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getBoolean(I_UNDERSTAND, false)
        set(value) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putBoolean(I_UNDERSTAND, value).apply()
        }

    override var baseCurrencyCode: String?
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(BASE_CURRENCY_CODE, null)
        set(value) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putString(BASE_CURRENCY_CODE, value).apply()
        }

    override var blockTillDate: Long?
        get() {
            val date = io.horizontalsystems.dynamitewallet.core.App.preferences.getLong(BLOCK_TILL_DATE, 0)
            return if (date > 0) date else null
        }
        set(date) {
            date?.let {
                io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putLong(BLOCK_TILL_DATE, date).apply()
            } ?: run {
                io.horizontalsystems.dynamitewallet.core.App.preferences.edit().remove(BLOCK_TILL_DATE).apply()
            }
        }

    override var failedAttempts: Int?
        get() {
            val attempts = io.horizontalsystems.dynamitewallet.core.App.preferences.getInt(FAILED_ATTEMPTS, 0)
            return when (attempts) {
                0 -> null
                else -> attempts
            }
        }
        set(value) {
            value?.let {
                io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putInt(FAILED_ATTEMPTS, it).apply()
            } ?: io.horizontalsystems.dynamitewallet.core.App.preferences.edit().remove(FAILED_ATTEMPTS).apply()
        }

    override var lockoutUptime: Long?
        get() {
            val timestamp = io.horizontalsystems.dynamitewallet.core.App.preferences.getLong(LOCKOUT_TIMESTAMP, 0L)
            return when (timestamp) {
                0L -> null
                else -> timestamp
            }
        }
        set(value) {
            value?.let {
                io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putLong(LOCKOUT_TIMESTAMP, it).apply()
            } ?: io.horizontalsystems.dynamitewallet.core.App.preferences.edit().remove(LOCKOUT_TIMESTAMP).apply()
        }

    override var baseBitcoinProvider: String?
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(BASE_BITCOIN_PROVIDER, null)
        set(value) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putString(BASE_BITCOIN_PROVIDER, value).apply()
        }

    override var baseEthereumProvider: String?
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(BASE_ETHEREUM_PROVIDER, null)
        set(value) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putString(BASE_ETHEREUM_PROVIDER, value).apply()
        }

    override var baseDashProvider: String?
        get() = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(BASE_DASH_PROVIDER, null)
        set(value) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putString(BASE_DASH_PROVIDER, value).apply()
        }

    override var syncMode: SyncMode
        get() {
            val syncString = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(SYNC_MODE, SyncMode.FAST.value)
            return syncString?.let { SyncMode.fromString(syncString) } ?: SyncMode.FAST
        }
        set(syncMode) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putString(SYNC_MODE, syncMode.value).apply()
        }

    override var sortType: BalanceSortType
        get() {
            val sortString = io.horizontalsystems.dynamitewallet.core.App.preferences.getString(SORT_TYPE, BalanceSortType.Default.getAsString()) ?: BalanceSortType.Default.getAsString()
            return BalanceSortType.getTypeFromString(sortString)
        }
        set(sortType) {
            io.horizontalsystems.dynamitewallet.core.App.preferences.edit().putString(SORT_TYPE, sortType.getAsString()).apply()
        }

    override fun clear() {
        io.horizontalsystems.dynamitewallet.core.App.preferences.edit().clear().apply()
    }

}
