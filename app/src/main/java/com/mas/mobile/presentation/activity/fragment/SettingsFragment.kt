package com.mas.mobile.presentation.activity.fragment

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.mas.mobile.MasApplication
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.SettingsFragmentBinding
import com.mas.mobile.presentation.viewmodel.SettingsViewModel
import com.mas.mobile.service.NotificationListener


class SettingsFragment : CommonFragment() {
    private lateinit var binding: SettingsFragmentBinding
    private val args: SettingsFragmentArgs by navArgs()
    private var permissionLauncher = this.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        settingsViewModel.captureSms.value = it
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

        settingsViewModel.period.observeForever { value ->
            binding.settingsPeriodLayout.setOnClickListener {
                showOptionsPicker(
                    getResourceService().messageSettingsPeriod(),
                    settingsViewModel.periodMap.values.toList(),
                    value
                ) { result ->
                    settingsViewModel.period.value = result
                }
            }
        }

        settingsViewModel.startDayOfMonth.observeForever { day ->
            binding.settingsStartDayOfMonth.setOnClickListener {
                showListPicker(
                    getResourceService().messageSettingsStartDay(),
                    settingsViewModel.availableDaysOfMonth,
                    day
                ) { value ->
                    settingsViewModel.startDayOfMonth.value = value
                }
            }
        }

        settingsViewModel.startDayOfWeek.observeForever { day ->
            binding.settingsStartDayOfWeek.setOnClickListener {
                showListPicker(
                    getResourceService().messageSettingsStartDay(),
                    settingsViewModel.availableDaysOfWeek,
                    day
                ) { value ->
                    settingsViewModel.startDayOfWeek.value = value
                }
            }
        }

        binding.settingsCaptureNotificationsLayout.setOnClickListener {
            binding.settingsCaptureNotifications.performClick()
        }

        binding.settingsCaptureSmsLayout.setOnClickListener {
            binding.settingsCaptureSms.performClick()
        }

        binding.settingsWhatIsLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(MasApplication.HOME_PAGE)
            startActivity(intent)
        }

        binding.settingsDiscoverySettingsLayout.setOnClickListener {
            go(SettingsFragmentDirections.actionToQualifierList())
        }
        binding.settingsDiscoverySettings.setOnClickListener {
            go(SettingsFragmentDirections.actionToQualifierList())
        }

        binding.settingsRulesLayout.setOnClickListener {
            go(SettingsFragmentDirections.actionToMessageRulesList())
        }
        binding.settingsRules.setOnClickListener {
            go(SettingsFragmentDirections.actionToMessageRulesList())
        }

        settingsViewModel.onRequestSMSPermissions { requestSMSPermissions() }
        settingsViewModel.onRequestNotificationPermissions { showNotificationSettings() }

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.guided) {
            blink(binding.settingsCaptureNotifications) {
                blink(binding.settingsCaptureSms)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
    }

    private fun blink(switch: SwitchCompat, after: () -> Unit = {}) {
        val originalDrawable = switch.background as? RippleDrawable
        val redRippleDrawable = RippleDrawable(
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)),
            null,
            null)

        switch.background = redRippleDrawable
        redRippleDrawable.state = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)

        switch.postDelayed(
            {
                switch.background = originalDrawable
                after()
            },
            850
        )
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

    private fun showOptionsPicker(title: String, list: List<String>, default: String?, result: (String) -> Unit) {
        val layout = LayoutInflater.from(this.requireContext()).inflate(R.layout.popup_options, null)
        val radioGroup = layout.findViewById<RadioGroup>(R.id.popupRadioGroup)

        list.forEachIndexed { index, value ->
            val button = RadioButton(layout.context).also {
                it.text = value
                it.id = index
            }
            radioGroup.addView(button)

            if (value == default) {
                radioGroup.check(index)
            }
        }

        AlertDialog.Builder(this.requireContext())
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(getResourceService().dialogConfirmationOk()) { _, _ ->
                val selectedId = radioGroup.checkedRadioButtonId
                val radioButton = radioGroup.findViewById<RadioButton>(selectedId)
                result(radioButton.text.toString())
            }
            .setNegativeButton(getResourceService().dialogConfirmationCancel()) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showListPicker(title: String, list: List<String>, default: String?, result: (String) -> Unit) {
        val numberPicker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = list.size - 1
            displayedValues = list.toTypedArray()
            value = list.indexOf(default).takeIf { it >= 0 } ?: 0
        }

        val builder = AlertDialog.Builder(requireContext()).also {
            it.setTitle(title)
            it.setView(numberPicker)
            it.setPositiveButton(getResourceService().dialogConfirmationOk()) { _, _ ->
                result(list[numberPicker.value])
            }
            it.setNegativeButton(getResourceService().dialogConfirmationCancel()) { dialog, _ ->
                dialog.dismiss()
            }
        }

        builder.create().show()
    }
}

