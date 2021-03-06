package ch.deletescape.lawnchair.preferences

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import com.android.quickstep.OverviewInteractionState
import com.android.systemui.shared.system.SettingsCompat

class SwipeUpSwitchPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : StyledSwitchPreferenceCompat(context, attrs) {

    private val securePrefName = SettingsCompat.SWIPE_UP_SETTING_NAME
    private val secureOverrideMode = OverviewInteractionState.isSwipeUpSettingsAvailable()
    private val hasWriteSecurePermission = ContextCompat.checkSelfPermission(context,
            android.Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    init {
        if (secureOverrideMode && !hasWriteSecurePermission) {
            isEnabled = false
        }
    }

    override fun shouldDisableDependents(): Boolean {
        return disableDependentsState == isChecked
    }

    override fun getPersistedBoolean(defaultReturnValue: Boolean): Boolean {
        if (secureOverrideMode) {
            try {
                return Settings.Secure.getInt(context.contentResolver, securePrefName) == 1
            } catch (ignored: Settings.SettingNotFoundException) {
            }
        }
        return super.getPersistedBoolean(defaultReturnValue)
    }

    override fun persistBoolean(value: Boolean): Boolean {
        if (hasWriteSecurePermission && secureOverrideMode) {
            try {
                return Settings.Secure.putInt(context.contentResolver, securePrefName, if (value) 1 else 0)
            } catch (ignored: Exception) {
            }
        }
        return super.persistBoolean(value)
    }

    override fun getSlice(context: Context, key: String): View {
        this.key = key
        return (super.getSlice(context, key) as Switch).apply {
            isChecked = getPersistedBoolean(true)
            setOnCheckedChangeListener { _, isChecked ->
                persistBoolean(isChecked)
            }
        }
    }
}
