package com.mas.mobile.presentation.activity.fragment

import android.Manifest
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.SettingsFragmentBinding
import com.mas.mobile.presentation.viewmodel.SettingsViewModel
import com.mas.mobile.service.BudgetService.Companion.TEMPLATE_BUDGET_ID
import com.mas.mobile.service.NotificationListener


class SettingsFragment : CommonFragment() {
    private lateinit var binding: SettingsFragmentBinding
    private var closeAction = { findNavController().popBackStack() }

    private val settingsViewModel: SettingsViewModel by lazyViewModel {
        this.requireContext().appComponent.settingsModel().create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.settings_fragment, container, false)

        binding = SettingsFragmentBinding.bind(layout)
        binding.settings = settingsViewModel
        binding.lifecycleOwner = this

        binding.settingsSaveButton.setOnClickListener {
            settingsViewModel.save()
            closeAction()
        }

        if (settingsViewModel.isThisFirstLaunch()) {
            handleFirstLaunch()
        }

        val requestPermissionLauncher = this.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    settingsViewModel.captureNotifications.value = true
                } else {
                    Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show()
                }
            }

        settingsViewModel.registerPermissionValidators(
            {
                requestSMSPermissions(requestPermissionLauncher)
            },
            {
                requestNotificationPermissions()
            }
        )

        binding.settingsRadiogroup.orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout.HORIZONTAL
            } else {
                LinearLayout.VERTICAL
            }

        return layout
    }

    private fun requestSMSPermissions(launcher: ActivityResultLauncher<String>) =
        when(ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.RECEIVE_SMS)) {
            PERMISSION_GRANTED -> {
                true
            }
            PERMISSION_DENIED -> {
                showSMSSettings()
                false
            }
            else -> {
                launcher.launch(Manifest.permission.RECEIVE_SMS)
                false
            }
        }

    private fun requestNotificationPermissions() =
        if (isNotifyServiceAllowed(this.requireContext())) {
            true
        } else {
            showNotificationSettings()
            false
        }

    private fun showNotificationSettings() {
        val component = ComponentName(this.requireContext(), NotificationListener::class.java)
        val intent = Intent(ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).also {
            it.putExtra(EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, component.flattenToString())
            it.flags = FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            showInfoDialog(getResourceService().messageAllowNotifications()) {}
        }
    }

    private fun showSMSSettings() {
        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.requireContext().packageName)).also {
            it.flags = FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            showInfoDialog(getResourceService().messageAllowSms()) {}
        }
    }

    private fun isNotifyServiceAllowed(context: Context): Boolean {
        val component = ComponentName(this.requireContext(), NotificationListener::class.java)
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.isNotificationListenerAccessGranted(component)
        } else {
            val flat = Secure.getString(context.contentResolver, "enabled_notification_listeners")
            flat != null && flat.contains(component.flattenToString())
        }
    }

    private fun handleFirstLaunch() {
        menuVisibility(false)
        showInfoDialog(getResourceService().messageSettingsFirstLaunch()) {}
        closeAction = {
            menuVisibility(true)
            go(SettingsFragmentDirections.actionToTemplateExpenditures(TEMPLATE_BUDGET_ID))
            true
        }
    }
}
