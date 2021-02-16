package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.databinding.ExpenditureListFragmentBinding
import com.mas.mobile.presentation.adapter.ExpenditureAdapter
import com.mas.mobile.presentation.listener.ListListener
import com.mas.mobile.presentation.viewmodel.ExpenditureListViewModel
import com.mas.mobile.presentation.viewmodel.NEW_ITEM
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Expenditure

open class ExpenditureListFragment: CommonFragment(), ListListener<Expenditure> {
    private val args: ExpenditureListFragmentArgs by navArgs()
    private val expenditureViewModel: ExpenditureListViewModel by viewModels {
        createWithFactory {
            ExpenditureListViewModel(requireActivity().application, args.budgetId)
        }
    }
    private lateinit var binding: ExpenditureListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.expenditure_list_fragment, container, false)

        val adapter = ExpenditureAdapter(this)
        binding = ExpenditureListFragmentBinding.bind(layout)
        binding.expenditureList.adapter = adapter
        binding.expenditureList.layoutManager = LinearLayoutManager(this.requireContext())

        expenditureViewModel.expenditures.observe(viewLifecycleOwner, { expenditure ->
            adapter.setExpenditure(expenditure)
        })

        binding.expenditureAddSpending.setOnClickListener {
            val dest = resolveAddButtonDestination()
            this.findNavController().navigate(dest)
        }

        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.standard_list_menu, menu)
    }

    override fun onRowItemClick(view: View, rowItem: Expenditure) {
        val dest = ExpenditureListFragmentDirections.actionToExpenditureSpending(Action.ADD.name, -1, rowItem.id)
        findNavController().navigate(dest)
    }

    override fun showRowMenu(): Int = 0

    override fun onRowMenuClick(item: MenuItem, rowItem: Expenditure): Boolean = true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_list_add -> {
                val dest = resolveAddButtonDestination()
                this.findNavController().navigate(dest)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected open fun resolveAddButtonDestination(): NavDirections {
        return ExpenditureListFragmentDirections.actionToExpenditureSpending(Action.ADD.name)
    }

}

class BudgetExpenditureListFragment: ExpenditureListFragment() {
    private val args: BudgetExpenditureListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addBudgetToTitle(args.budgetId)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun showRowMenu(): Int = R.menu.expenditure_row_menu

    override fun onRowMenuClick(item: MenuItem, rowItem: Expenditure): Boolean {
        return when (item.itemId) {
            R.id.nav_expenditure_list_row_spend -> {
                //findNavController().navigate(R.id.action_expenditure_list_to_spending)
                true
            }
            R.id.nav_expenditure_list_row_report -> {
                //findNavController().navigate(R.id.action_expenditure_list_to_spending_list)
                true
            }
            else -> false
        }
    }

    override fun onRowItemClick(view: View, rowItem: Expenditure) {
        val dest = BudgetExpenditureListFragmentDirections
            .actionToBudgetExpenditure(Action.VIEW.name, rowItem.id, args.budgetId)
        findNavController().navigate(dest)
    }

    override fun resolveAddButtonDestination(): NavDirections {
        return BudgetExpenditureListFragmentDirections
            .actionToBudgetExpenditure(Action.ADD.name, NEW_ITEM, args.budgetId)
    }
}