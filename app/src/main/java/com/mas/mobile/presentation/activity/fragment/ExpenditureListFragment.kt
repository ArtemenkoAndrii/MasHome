package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.ExpenditureListFragmentBinding
import com.mas.mobile.presentation.adapter.BudgetExpenditureAdapter
import com.mas.mobile.presentation.adapter.ExpenditureAdapter
import com.mas.mobile.presentation.viewmodel.ExpenditureListViewModel
import com.mas.mobile.presentation.viewmodel.NEW_ITEM
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Expenditure

open class ExpenditureListFragment : BaseListFragment() {
    private val args: ExpenditureListFragmentArgs by navArgs()
    protected lateinit var binding: ExpenditureListFragmentBinding

    protected val expenditureViewModel: ExpenditureListViewModel by lazyViewModel {
        this.requireContext().appComponent.expenditureListViewModel().create(args.budgetId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.expenditure_list_fragment, container, false)

        val adapter = getAdapter()
        binding = ExpenditureListFragmentBinding.bind(layout)
        binding.expenditureList.adapter = adapter
        binding.expenditureList.layoutManager = LinearLayoutManager(this.requireContext())
        binding.model = expenditureViewModel
        binding.lifecycleOwner = this

        expenditureViewModel.expenditures.observe(viewLifecycleOwner) { expenditure ->
            adapter.setItems(expenditure)
        }

        binding.expenditureAddSpending.setOnClickListener {
            go(resolveAddButtonDestination())
        }

        return layout
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.standartd_list_menu_add -> {
                val dest = resolveAddButtonDestination()
                this.findNavController().navigate(dest)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun resolveAddButtonDestination() =
        ExpenditureListFragmentDirections.actionToExpenditureSpending(Action.ADD.name)

    protected open fun getAdapter() = ExpenditureAdapter()
}

class BudgetExpenditureListFragment: ExpenditureListFragment() {
    private val args: BudgetExpenditureListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideBottomMenu()
        setTitle { "${expenditureViewModel.budgetName} $it" }
        val layout = super.onCreateView(inflater, container, savedInstanceState)

        if (expenditureViewModel.isFirstLaunchSession()) {
            handleFirstLaunch()
        }
        return layout
    }

    override fun onDestroy() {
        showBottomMenu()
        super.onDestroy()
    }

    override fun resolveAddButtonDestination(): NavDirections {
        return BudgetExpenditureListFragmentDirections
            .actionToBudgetExpenditure(Action.ADD.name, NEW_ITEM, args.budgetId)
    }

    override fun getAdapter() = BudgetExpenditureAdapter(this)

    private fun handleFirstLaunch() {
        binding.expenditureListProgress.visibility = View.GONE
        binding.expenditureListFinish.visibility = View.VISIBLE
        binding.expenditureListFinish.setOnClickListener {
            showBottomMenu()
            this.findNavController().navigate(R.id.nav_expenditure_list)
        }
        if (expenditureViewModel.isFirstLaunchInfo()) {
            showInfoDialog(getResourceService().messageExpenditureFirstLaunch()) {}
        }
    }

    fun removeItem(item: Expenditure) {
        showConfirmationDialog {
            expenditureViewModel.remove(item)
        }
    }
}
