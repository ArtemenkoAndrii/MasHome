package com.mas.mobile.presentation.adapter

import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.iterator
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.BudgetListRowBinding
import com.mas.mobile.domain.budget.Budget
import com.mas.mobile.presentation.activity.fragment.BudgetListFragmentDirections
import com.mas.mobile.presentation.activity.fragment.ListMenu
import com.mas.mobile.presentation.viewmodel.validator.Action


class BudgetAdapter(private val listMenu: ListMenu<Budget>): BaseAdapter<Budget, BudgetListRowBinding>(R.layout.budget_list_row) {

    override fun bind(binding: BudgetListRowBinding, item: Budget, prior: Budget?) {
        binding.budget = item

        val rowView = binding.budgetRowLayout

        binding.callback = View.OnClickListener { viewMenu ->
            with(PopupMenu(viewMenu.context, viewMenu)) {
                inflate(R.menu.budget_row_menu)

                if (item.id.value == Budget.TEMPLATE_ID) {
                    findRemoveButton(menu, viewMenu)?.let { it.isEnabled = false }
                }

                setOnMenuItemClickListener {
                    listMenu.onRowMenuSelected(it, item)
                }
                show()
            }
        }

        binding.budgetRowLayout.setOnClickListener {
            val action = BudgetListFragmentDirections
                .actionToBudget(Action.VIEW.name, item.id.value)
            rowView.findNavController().navigate(action)
        }
    }

    override fun getBinding(view: View) = BudgetListRowBinding.bind(view)

    private fun findRemoveButton(menu: Menu, view: View): MenuItem? {
        val label = view.context.getString(R.string.menu_remove)
        return menu.iterator().asSequence().firstOrNull { it.title == label }
    }
}