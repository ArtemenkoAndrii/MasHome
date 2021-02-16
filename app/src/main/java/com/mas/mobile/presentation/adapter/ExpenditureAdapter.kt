package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.ExpenditureListRowBinding
import com.mas.mobile.presentation.activity.fragment.ExpenditureListFragmentDirections

import com.mas.mobile.presentation.viewmodel.NEW_ITEM
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Expenditure

open class ExpenditureAdapter: BaseAdapter<Expenditure, ExpenditureListRowBinding>(R.layout.expenditure_list_row) {
    override fun bind(binding: ExpenditureListRowBinding, item: Expenditure, prior: Expenditure?) {
        binding.expenditureRowMenu.visibility = View.GONE
        binding.expenditure = item
        binding.expenditureRowLayout.setOnClickListener {
            ExpenditureListFragmentDirections.actionToExpenditureSpending(
                Action.ADD.name,
                NEW_ITEM,
                item.id
            ).also {
                binding.expenditureRowLayout.findNavController().navigate(it)
            }
        }
    }

    override fun getBinding(view: View) = ExpenditureListRowBinding.bind(view)
}