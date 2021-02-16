package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.databinding.SpendingListFragmentBinding
import com.mas.mobile.presentation.adapter.SpendingAdapter
import com.mas.mobile.presentation.viewmodel.NEW_ITEM
import com.mas.mobile.presentation.viewmodel.SpendingListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

open class SpendingListFragment : CommonFragment() {
    private val args: SpendingListFragmentArgs by navArgs()
    private val spendingViewModel: SpendingListViewModel by viewModels {
        createWithFactory {
            SpendingListViewModel(requireActivity().application, args.budgetId)
        }
    }
    private lateinit var binding: SpendingListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.spending_list_fragment, container, false)

        val adapter = SpendingAdapter(spendingViewModel)
        binding = SpendingListFragmentBinding.bind(layout)
        binding.spendingList.adapter = adapter
        binding.spendingList.layoutManager = LinearLayoutManager(this.requireContext())

        spendingViewModel.spendings.observe(viewLifecycleOwner, { spending ->
            adapter.setItems(spending)
        })

        binding.spendingAddBtn.setOnClickListener {
            val action = resolveAddButtonDestination()
            this.findNavController().navigate(action)
        }

        return layout
    }

    open fun resolveAddButtonDestination(): NavDirections {
        return SpendingListFragmentDirections.actionToSpending(Action.ADD.name)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.standard_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_list_add -> {
                val action = resolveAddButtonDestination()
                this.findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        addBudgetToTitle(args.budgetId)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}