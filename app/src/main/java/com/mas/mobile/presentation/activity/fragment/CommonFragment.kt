package com.mas.mobile.presentation.activity.fragment

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.service.ResourceService
import javax.inject.Inject

abstract class CommonFragment : Fragment() {
    private lateinit var wrapper: CommonFragmentWrapper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wrapper = CommonFragmentWrapper()
        context.appComponent.injectCommonFragment(wrapper)
    }

    class CommonFragmentWrapper() {
        @Inject
        lateinit var resourceService: ResourceService
    }

    protected fun getResourceService() = wrapper.resourceService

    protected fun showConfirmationDialog(
        message: String = getResourceService().messageAreYouSure(),
        confirm: ()-> Unit) {

        AlertDialog.Builder(this.requireContext()).also {
            it.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_confirmation_yes) { _, _ -> confirm() }
                .setNegativeButton(R.string.dialog_confirmation_no) { _, _ -> }
        }.create().show()
    }

    protected fun showInfoDialog(message: String, confirm: ()-> Unit) {
        AlertDialog.Builder(this.requireContext()).also {
            it.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_confirmation_gotit) { _, _ -> confirm() }
        }.create().show()
    }

    protected fun go(direction: NavDirections) {
        this.findNavController().navigate(direction)
    }

    protected fun menuVisibility(isEnabled: Boolean) {
        this.activity?.let {
            val menu = it.findViewById<BottomNavigationView>(R.id.nav_bottom_view).menu
            menu.setGroupVisible(0, isEnabled)
            it.findViewById<BottomNavigationView>(R.id.toolbar).isVisible = isEnabled
        }
    }
}