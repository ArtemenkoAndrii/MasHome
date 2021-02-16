package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mas.mobile.R
import com.mas.mobile.databinding.BudgetFragmentBinding
import com.mas.mobile.presentation.adapter.AutoCompleteAdapter
import com.mas.mobile.presentation.viewmodel.BudgetViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class BudgetFragment: CommonFragment() {
    private lateinit var binding: BudgetFragmentBinding
    private val args: BudgetFragmentArgs by navArgs()
    private val budgetViewModel: BudgetViewModel by viewModels {
        createWithFactory {
            BudgetViewModel(requireActivity().application,
                args.budgetId,
                action != Action.VIEW)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.budget_fragment, container, false)

        action = Action.valueOf(args.action)

        binding = BudgetFragmentBinding.bind(layout)
        binding.budget = budgetViewModel
        binding.lifecycleOwner = this

        binding.budgetSaveButton.setOnClickListener {
            handleSaveAndClose(budgetViewModel)
        }

        binding.budgetStartsOn.setOnClickListener {
            showDateDialog(binding.budgetStartsOn)
        }

        budgetViewModel.availableBudgets.observe(viewLifecycleOwner, {
            val adapter = AutoCompleteAdapter(this.requireContext(), it )
            binding.budgetBase.setAdapter(adapter)

            binding.budgetBase.setOnItemClickListener { _, view, _, _ ->
                val textView = view.findViewById<TextView>(R.id.autocompleteName)
                val budgetId = (textView.tag ?: 0) as Int
                budgetViewModel.basedOnId.value = budgetId
            }
        })

        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.budget_form_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_budget_form_edit -> {
                budgetViewModel.enableEditing()
                action = Action.EDIT
                true
            }
            R.id.nav_budget_form_clone -> {
                budgetViewModel.enableEditing()
                action = Action.CLONE
                true
            }
            R.id.nav_budget_form_expenditures -> {
                val dest = BudgetFragmentDirections.actionToBudgetExpenditureList(args.budgetId)
                this.findNavController().navigate(dest)
                true
            }
            R.id.nav_budget_form_spendings-> {
                val dest = BudgetListFragmentDirections.actionToBudgetSpendingList(args.budgetId)
                this.findNavController().navigate(dest)
                true
            }
            R.id.nav_budget_form_remove -> {
                budgetViewModel.save(Action.REMOVE)
                this.findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}