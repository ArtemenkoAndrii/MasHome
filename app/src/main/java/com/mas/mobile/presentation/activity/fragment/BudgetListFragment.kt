package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.databinding.BudgetListFragmentBinding
import com.mas.mobile.presentation.adapter.BudgetAdapter
import com.mas.mobile.presentation.viewmodel.BudgetListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class BudgetListFragment: CommonFragment() {
    private val budgetListViewModel: BudgetListViewModel by viewModels()
    private lateinit var binding: BudgetListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.budget_list_fragment, container, false)

        val adapter = BudgetAdapter(budgetListViewModel)
        binding = BudgetListFragmentBinding.bind(layout)
        binding.budgetList.adapter = adapter
        binding.budgetList.layoutManager = LinearLayoutManager(this.requireContext())

        budgetListViewModel.budgets.observe(viewLifecycleOwner, { budget ->
            adapter.setItems(budget)
        })

        binding.budgetAddBtn.setOnClickListener {
            val dest = BudgetListFragmentDirections.actionToBudget(Action.ADD.name)
            this.findNavController().navigate(dest)
        }

        return layout
    }

}