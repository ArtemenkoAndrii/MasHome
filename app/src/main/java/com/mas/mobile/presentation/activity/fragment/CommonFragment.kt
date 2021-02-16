package com.mas.mobile.presentation.activity.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.mas.mobile.R
import com.mas.mobile.presentation.activity.MainActivity
import com.mas.mobile.presentation.viewmodel.CommonViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.util.DateTool
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

abstract class CommonFragment: Fragment() {
    protected lateinit var action: Action

    protected fun addBudgetToTitle(budgetId: Int) {
        val bar = (this.requireActivity() as MainActivity).supportActionBar
        if (bar != null) {
            bar.title = "[${getBudgetName(budgetId)}] ${bar.title}"
        }
    }

    protected fun createWithFactory(create: () -> ViewModel): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return create.invoke() as T
            }
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

    protected fun showConfirmationDialog(message: String) {
        val builder = AlertDialog.Builder(this.requireContext())
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                // Delete selected note from database
            }
            .setNegativeButton("No") { _, _ ->
                // Dismiss the dialog
            }
        builder.create().show()
    }

    private fun getBudgetName(budgetId: Int): String {
        BudgetRepository.initDb(this.requireContext())
        val budget = if (budgetId > 0) {
            BudgetRepository.getById(budgetId)
        } else {
            BudgetRepository.getActive()
        }

        return budget.name
    }

    fun handleSaveAndClose(viewModel: CommonViewModel<out Any>) {
        if (viewModel.save(action)) {
            findNavController().popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.standard_form_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_form_edit -> {
                onEdit()
                action = Action.EDIT
                true
            }
            R.id.nav_form_clone -> {
                onClone()
                action = Action.CLONE
                true
            }
            R.id.nav_form_remove -> {
                onRemove()
                action = Action.REMOVE
                this.findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    open fun onEdit(){}
    open fun onClone(){}
    open fun onRemove(){}

}