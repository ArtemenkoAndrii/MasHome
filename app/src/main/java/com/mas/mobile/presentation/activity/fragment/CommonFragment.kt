package com.mas.mobile.presentation.activity.fragment

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.service.ResourceService
import javax.inject.Inject

abstract class CommonFragment : Fragment() {
    private lateinit var wrapper: CommonFragmentWrapper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wrapper = CommonFragmentWrapper()
        context.appComponent.injectCommonFragment(wrapper)
    }

    class CommonFragmentWrapper {
        @Inject
        lateinit var resourceService: ResourceService
        @Inject
        lateinit var budgetService: BudgetService
    }

    fun getResourceService() = wrapper.resourceService
    fun getBudgetService() = wrapper.budgetService

    fun showConfirmationDialog(
        message: String = getResourceService().messageAreYouSure(),
        confirm: () -> Unit
    ) {
        showConfirmationDialog(message, confirm) {}
    }

    fun showConfirmationDialog(
        message: String = getResourceService().messageAreYouSure(),
        confirm: () -> Unit,
        reject: () -> Unit,
    ) {

        AlertDialog.Builder(this.requireContext()).also {
            it.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_confirmation_yes) { _, _ -> confirm() }
                .setNegativeButton(R.string.dialog_confirmation_no) { _, _ -> reject() }
        }.create().show()
    }

    fun showInfoDialog(message: String, confirm: ()-> Unit) {
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
        this.activity?.let { activity ->
            activity.findViewById<BottomNavigationView>(R.id.nav_bottom_view)
                ?.menu?.setGroupVisible(0, isEnabled)
            activity.findViewById<View>(R.id.toolbar)?.isVisible = isEnabled
        }
    }
}