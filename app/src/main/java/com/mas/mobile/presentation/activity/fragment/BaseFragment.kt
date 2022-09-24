package com.mas.mobile.presentation.activity.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.mas.mobile.R
import com.mas.mobile.presentation.activity.MainActivity
import com.mas.mobile.presentation.viewmodel.BaseViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.util.DateTool
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

abstract class BaseFragment<V: BaseViewModel<*>>: CommonFragment() {
    protected lateinit var action: Action
    abstract fun getViewModel(): V

    protected fun addBudgetToTitle(budgetId: Int) {
        val bar = (this.requireActivity() as MainActivity).supportActionBar
        if (bar != null) {
            bar.title = "[${getBudgetName(budgetId)}] ${bar.title}"
        }
    }

    protected fun showDateDialog(editor: TextInputEditText) {
        val dialog =
            DatePickerFragment.newInstance { _, year, month, day ->
                val date = LocalDate.of(year, month + 1, day)
                editor.setText(DateTool.dateToString(date))
            }

        dialog.show(requireActivity().supportFragmentManager, "dateTimePicker")
    }

    protected fun showDateTimeDialog(editor: TextInputEditText) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val dateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                val result = DateTool.dateTimeToString(dateTime)
                editor.setText(result)
            }, startHour, startMinute, true).show()
        }, startYear, startMonth, startDay).show()
    }

    private fun getBudgetName(budgetId: Int): String {
        return "FIX ME"
    }

    fun saveAndClose(viewModel: BaseViewModel<out Any>) {
        if (viewModel.save()) {
            findNavController().popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.standard_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.standard_menu_edit -> {
                onEdit()
                action = Action.EDIT
                true
            }
            R.id.standard_menu_remove -> {
                onRemove()
                action = Action.REMOVE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    open fun onEdit() {
        getViewModel().enableEditing()
    }

    open fun onRemove() {
        showConfirmationDialog(getLabel(R.string.dialog_confirmation_remove)) {
            getViewModel().remove()
            this.findNavController().popBackStack()
        }
    }

    private fun getLabel(resourceId: Int) =
        context?.getString(R.string.dialog_confirmation_remove) ?: ""
}