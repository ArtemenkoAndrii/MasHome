package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.BudgetListRowBinding
import com.mas.mobile.presentation.activity.fragment.BudgetListFragmentDirections
import com.mas.mobile.presentation.viewmodel.BudgetListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Budget

class BudgetAdapter(private val budgetListViewModel: BudgetListViewModel): CommonAdapter<Budget>(R.layout.budget_list_row) {
    override fun bind(item: Budget, prior: Budget?, rowView: View) {
        val binding = BudgetListRowBinding.bind(rowView)
        binding.budget = item

        binding.callback = View.OnClickListener{ viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.budget_row_menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.nav_budget_row_edit -> {
                        val action = BudgetListFragmentDirections
                            .actionToBudget(Action.EDIT.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.nav_budget_row_clone -> {
                        val action = BudgetListFragmentDirections
                            .actionToBudget(Action.CLONE.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.nav_budget_row_expenditures -> {
                        val action = BudgetListFragmentDirections
                            .actionToBudgetExpenditureList(item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.nav_budget_row_spendings -> {
                        val action = BudgetListFragmentDirections
                            .actionToBudgetSpendingList(item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.nav_budget_row_remove -> {
                        budgetListViewModel.remove(item)
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }

        binding.budgetRowLayout.setOnClickListener {
            val action = BudgetListFragmentDirections
                .actionToBudget(Action.VIEW.name, rowView.id)
            rowView.findNavController().navigate(action)
        }
    }
}