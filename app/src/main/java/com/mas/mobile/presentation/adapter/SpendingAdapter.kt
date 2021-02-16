package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.SpendingListRowBinding
import com.mas.mobile.presentation.activity.fragment.SpendingListFragment
import com.mas.mobile.presentation.activity.fragment.SpendingListFragmentDirections
import com.mas.mobile.presentation.viewmodel.SpendingListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Spending

class SpendingAdapter(
    private val fragment: SpendingListFragment
): BaseAdapter<Spending, SpendingListRowBinding>(R.layout.spending_list_row) {

    override fun bind(binding: SpendingListRowBinding, item: Spending, prior: Spending?) {
        val rowView = binding.spendingListRowLayout
        binding.spending = item
        if (prior?.date?.toLocalDate() == item.date.toLocalDate()) {
            binding.spendingListRowDate.visibility = View.GONE
        }
        binding.callback = View.OnClickListener { viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.standard_row_menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.standard_row_menu_view -> {
                        val action = SpendingListFragmentDirections.actionToSpending(Action.VIEW.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.standard_row_menu_edit -> {
                        val action = SpendingListFragmentDirections.actionToSpending(Action.EDIT.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.standard_row_menu_clone -> {
                        val action = SpendingListFragmentDirections.actionToSpending(Action.CLONE.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.standard_row_menu_remove -> {
                        fragment.removeItem(item)
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }

        binding.spendingListRowLayout.setOnClickListener {
            val action = SpendingListFragmentDirections.actionToSpending(Action.VIEW.name, item.id)
            rowView.findNavController().navigate(action)
        }
    }

    override fun getBinding(view: View) = SpendingListRowBinding.bind(view)
}