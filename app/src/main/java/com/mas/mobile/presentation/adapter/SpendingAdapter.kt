package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.SpendingListRowBinding
import com.mas.mobile.presentation.activity.fragment.SpendingListFragmentDirections
import com.mas.mobile.presentation.viewmodel.SpendingListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Spending

class SpendingAdapter(private val spendingListViewModel: SpendingListViewModel): CommonAdapter<Spending>(R.layout.spending_list_row) {

    override fun bind(item: Spending, prior: Spending?, rowView: View) {
        val binding = SpendingListRowBinding.bind(rowView)
        binding.spending = item

        if (prior?.date?.toLocalDate() == item.date.toLocalDate()) {
            binding.spendingListRowDate.visibility = View.GONE
        }

        binding.callback = View.OnClickListener{ viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.standard_row_menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.nav_row_view -> {
                        val action = SpendingListFragmentDirections.actionToSpending(Action.VIEW.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.nav_row_edit -> {
                        val action = SpendingListFragmentDirections.actionToSpending(Action.EDIT.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.nav_row_clone -> {
                        val action = SpendingListFragmentDirections.actionToSpending(Action.CLONE.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.nav_row_remove -> {
                        spendingListViewModel.remove(item)
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
}