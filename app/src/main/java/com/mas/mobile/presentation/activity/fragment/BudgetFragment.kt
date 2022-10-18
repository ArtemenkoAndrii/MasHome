package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.BudgetFragmentBinding
import com.mas.mobile.presentation.viewmodel.BudgetViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.util.DateTool

class BudgetFragment : BaseFragment<BudgetViewModel>() {
    private lateinit var binding: BudgetFragmentBinding
    private val args: BudgetFragmentArgs by navArgs()

    private val budgetViewModel: BudgetViewModel by lazyViewModel {
        this.requireContext().appComponent.budgetViewModel()
            .create(args.budgetId, action)
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
            saveAndClose(budgetViewModel)
        }

        binding.budgetLastDayAt.setOnClickListener {
            if (budgetViewModel.isChangeable()) {
                showDateDialog(
                    startDate= budgetViewModel.lastDayAtValue,
                    minDate = budgetViewModel.startsOnValue
                ) {
                    binding.budgetLastDayAt.setText(DateTool.dateToString(it))
                }
            } else {
                showInfoDialog(getResourceService().budgetChangeDateMessage()) { }
            }
        }

        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.budget_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.budget_menu_edit -> {
                budgetViewModel.enableEditing()
                action = Action.EDIT
                true
            }
            R.id.budget_menu_remove -> showConfirmationDialog {
                budgetViewModel.remove()
                findNavController().popBackStack()
            }.let { true }
            R.id.budget_menu_expenditure_list -> go(BudgetFragmentDirections.actionToBudgetExpenditureList(args.budgetId)).let { true }
            R.id.budget_menu_spending_list-> go(BudgetListFragmentDirections.actionToBudgetSpendingList(args.budgetId)).let { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getViewModel() = budgetViewModel
}
