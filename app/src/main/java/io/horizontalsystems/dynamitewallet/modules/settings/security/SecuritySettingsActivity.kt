package io.horizontalsystems.dynamitewallet.modules.settings.security

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.dynamitewallet.BaseActivity
import io.horizontalsystems.dynamitewallet.R
import io.horizontalsystems.dynamitewallet.core.App
import io.horizontalsystems.dynamitewallet.entities.BiometryType
import io.horizontalsystems.dynamitewallet.lib.AlertDialogFragment
import io.horizontalsystems.dynamitewallet.modules.backup.BackupModule
import io.horizontalsystems.dynamitewallet.modules.backup.BackupPresenter
import io.horizontalsystems.dynamitewallet.modules.main.MainModule
import io.horizontalsystems.dynamitewallet.modules.pin.PinModule
import io.horizontalsystems.dynamitewallet.modules.restore.RestoreModule
import io.horizontalsystems.dynamitewallet.ui.dialogs.BottomButtonColor
import io.horizontalsystems.dynamitewallet.ui.dialogs.BottomConfirmAlert
import io.horizontalsystems.dynamitewallet.ui.extensions.TopMenuItem
import kotlinx.android.synthetic.main.activity_settings_security.*

class SecuritySettingsActivity : io.horizontalsystems.dynamitewallet.BaseActivity(), BottomConfirmAlert.Listener {

    private lateinit var viewModel: SecuritySettingsViewModel

    enum class Action {
        OPEN_RESTORE,
        CLEAR_WALLETS
    }

    private var selectedAction: Action? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(SecuritySettingsViewModel::class.java)
        viewModel.init()

        setContentView(R.layout.activity_settings_security)

        shadowlessToolbar.bind(
                title = getString(R.string.Settings_SecurityCenter),
                leftBtnItem = TopMenuItem(R.drawable.back, { onBackPressed() })
        )

        changePin.apply {
            showArrow()
            setOnClickListener { viewModel.delegate.didTapEditPin() }
        }

        backupWallet.apply {
            showArrow()
            setOnClickListener { viewModel.delegate.didTapBackupWallet() }
        }


        importWallet.setOnClickListener {
            selectedAction = Action.OPEN_RESTORE
            val confirmationList = mutableListOf(
                    R.string.SettingsSecurity_ImportWalletConfirmation_1,
                    R.string.SettingsSecurity_ImportWalletConfirmation_2
            )
            BottomConfirmAlert.show(this, confirmationList, this)
        }

        unlink.setOnClickListener {
            selectedAction = Action.CLEAR_WALLETS
            val confirmationList = mutableListOf(
                    R.string.SettingsSecurity_ImportWalletConfirmation_1,
                    R.string.SettingsSecurity_ImportWalletConfirmation_2
            )
            BottomConfirmAlert.show(this, confirmationList, this, BottomButtonColor.RED)
        }

        unlink.titleTextColor = R.color.red_warning


        viewModel.backedUpLiveData.observe(this, Observer { wordListBackedUp ->
            wordListBackedUp?.let { wordListIsBackedUp ->
                backupWallet.setInfoBadgeVisibility(!wordListIsBackedUp)
            }
        })

        viewModel.openEditPinLiveEvent.observe(this, Observer {
            PinModule.startForEditPin(this)
        })

        viewModel.openRestoreWalletLiveEvent.observe(this, Observer {
            RestoreModule.start(this)
        })

        viewModel.openBackupWalletLiveEvent.observe(this, Observer {
            BackupModule.start(this@SecuritySettingsActivity, BackupPresenter.DismissMode.DISMISS_SELF)
        })

        viewModel.biometryTypeLiveDate.observe(this, Observer { biometryType ->
            fingerprint.visibility = if (biometryType == BiometryType.FINGER) View.VISIBLE else View.GONE
        })

        viewModel.biometricUnlockOnLiveDate.observe(this, Observer { switchIsOn ->
            switchIsOn?.let { switchOn ->
                fingerprint.apply {
                    switchIsChecked = switchOn
                    setOnClickListener {
                        if (io.horizontalsystems.dynamitewallet.core.App.localStorage.isBiometricOn || fingerprintCanBeEnabled()) {
                            switchToggle()
                        }
                    }

                    switchOnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
                        io.horizontalsystems.dynamitewallet.core.App.localStorage.isBiometricOn = isChecked
                    }
                }
            }
        })

        viewModel.reloadAppLiveEvent.observe(this, Observer {
            MainModule.startAsNewTask(this)
        })

        viewModel.showPinUnlockLiveEvent.observe(this, Observer {
            PinModule.startForUnlock(this,true)
        })

    }

    override fun onConfirmationSuccess() {
        when (selectedAction) {
            Action.OPEN_RESTORE -> viewModel.delegate.didTapRestoreWallet()
            Action.CLEAR_WALLETS -> viewModel.delegate.confirmedUnlinkWallet()
        }
    }

    private fun fingerprintCanBeEnabled(): Boolean {
        val touchSensorCanBeUsed = io.horizontalsystems.dynamitewallet.core.App.systemInfoManager.touchSensorCanBeUsed()
        if (!touchSensorCanBeUsed) {
            AlertDialogFragment.newInstance(R.string.Settings_Error_FingerprintNotEnabled, R.string.Settings_Error_NoFingerprintAddedYet, R.string.Alert_Ok)
                    .show(this.supportFragmentManager, "fingerprint_not_enabled_alert")
            return false
        }
        return true
    }
}
