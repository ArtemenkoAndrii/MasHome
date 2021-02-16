package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.ExpenditureListRowBinding
import com.mas.mobile.presentation.activity.fragment.BudgetExpenditureListFragment
import com.mas.mobile.presentation.activity.fragment.BudgetExpenditureListFragmentDirections
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Expenditure

class BudgetExpenditureAdapter(private val fragment: BudgetExpenditureListFragment): ExpenditureAdapter() {
    override fun bind(binding: ExpenditureListRowBinding, item: Expenditure, prior: Expenditure?) {
        binding.expenditure = item

        binding.callback = View.OnClickListener { viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.standard_row_menu)
            menu.setOnMenuItemClickListener { menuItem ->
                val controller = binding.expenditureRowLayout.findNavController()
                when (menuItem.itemId) {
                    R.id.standard_row_menu_edit ->
                        BudgetExpenditureListFragmentDirections.actionToBudgetExpenditure(
                            Action.EDIT.name,
                            item.id,
                            item.budget.id
                        ).also { controller.navigate(it) }
                    R.id.standard_row_menu_clone ->
                        BudgetExpenditureListFragmentDirections.actionToBudgetExpenditure(
                            Action.CLONE.name,
                            item.id,
                            item.budget.id
                        ).also { controller.navigate(it) }
                    R.id.standard_row_menu_remove ->
                        fragment.removeItem(item)
                    else -> view(item).also { controller.navigate(it) }
                }
                true
            }
            menu.show()
        }

        binding.expenditureRowLayout.setOnClickListener {
            binding.expenditureRowLayout.findNavController().navigate(view(item))
        }
    }

    private fun view(item: Expenditure) =
        BudgetExpenditureListFragmentDirections.actionToBudgetExpenditure(
            Action.VIEW.name,
            item.id,
            item.budget.id
        )
}