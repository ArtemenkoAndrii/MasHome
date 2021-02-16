package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.SpendingListFragmentBinding
import com.mas.mobile.presentation.adapter.SpendingAdapter
import com.mas.mobile.presentation.viewmodel.NEW_ITEM
import com.mas.mobile.presentation.viewmodel.SpendingListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Spending

open class SpendingListFragment : BaseListFragment() {
    private val args: SpendingListFragmentArgs by navArgs()
    private lateinit var binding: SpendingListFragmentBinding

    protected val spendingViewModel: SpendingListViewModel by lazyViewModel {
        this.requireContext().appComponent.spendingListViewModel().create(args.budgetId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.spending_list_fragment, container, false)

        val adapter = SpendingAdapter(this)
        binding = SpendingListFragmentBinding.bind(layout)
        binding.spendingList.adapter = adapter
        binding.spendingList.layoutManager = LinearLayoutManager(this.requireContext())

        spendingViewModel.spendings.observe(viewLifecycleOwner) { spending ->
            adapter.setItems(spending)
        }

        binding.spendingAddBtn.setOnClickListener {
            val action = resolveAddButtonDestination()
            this.findNavController().navigate(action)
        }

        return layout
    }

    override fun onDestroy() {
        showBottomMenu()
        super.onDestroy()
    }

    override fun resolveAddButtonDestination() =
        SpendingListFragmentDirections.actionToSpending(Action.ADD.name)

    fun removeItem(item: Spending) {
        showConfirmationDialog() {
            spendingViewModel.remove(item)
        }
    }
}

class BudgetSpendingListFragment: SpendingListFragment() {
    private val args: BudgetSpendingListFragmentArgs by navArgs()

    override fun resolveAddButtonDestination(): NavDirections {
        return BudgetSpendingListFragmentDirections.actionToSpending(Action.ADD.name, NEW_ITEM, -1, args.budgetId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideBottomMenu()
        setTitle { "${spendingViewModel.budgetName} $it" }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        showBottomMenu()
        super.onDestroy()
    }
}