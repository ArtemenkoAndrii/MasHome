package com.mas.mobile.presentation.activity.fragment

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.SettingsFragmentBinding
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.presentation.viewmodel.SettingsViewModel
import com.mas.mobile.service.NotificationListener


class SettingsFragment : CommonFragment() {
    private lateinit var binding: SettingsFragmentBinding
    private var closeAction = { findNavController().popBackStack() }
    private var permissionLauncher = this.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        settingsViewModel.captureNotifications.value = it
    }

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

        settingsViewModel.onRequestSMSPermissions { requestSMSPermissions() }
        settingsViewModel.onRequestNotificationPermissions { showNotificationSettings() }

        binding.settingsRadiogroup.orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout.HORIZONTAL
            } else {
                LinearLayout.VERTICAL
            }

        return layout
    }

    private fun requestSMSPermissions() {
        permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
    }

    private fun showNotificationSettings() {
        val component = ComponentName(this.requireContext(), NotificationListener::class.java)
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).also {
            it.putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, component.flattenToString())
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            this.requireContext().startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            showInfoDialog(getResourceService().messageAllowNotifications()) {}
        }
    }

    private fun handleFirstLaunch() {
        menuVisibility(false)
        showInfoDialog(getResourceService().messageSettingsFirstLaunch()) {}
        closeAction = {
            menuVisibility(true)
            go(SettingsFragmentDirections.actionToTemplateExpenditures(BudgetService.TEMPLATE_BUDGET_ID))
            true
        }
    }
}
