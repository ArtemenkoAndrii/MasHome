package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.ExpenditureFragmentBinding
import com.mas.mobile.presentation.adapter.AutoCompleteAdapter
import com.mas.mobile.presentation.viewmodel.ExpenditureViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class ExpenditureFragment : BaseFragment<ExpenditureViewModel>() {
    private lateinit var binding: ExpenditureFragmentBinding
    private val args: ExpenditureFragmentArgs by navArgs()

    private val expenditureViewModel: ExpenditureViewModel by lazyViewModel {
        this.requireContext().appComponent.expenditureViewModel().create(
            args.expenditureId,
            args.budgetId,
            action
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.expenditure_fragment, container, false)

        action = Action.valueOf(args.action)

        binding = ExpenditureFragmentBinding.bind(layout)
        binding.expenditure = expenditureViewModel
        binding.lifecycleOwner = this

        binding.expenditureSaveButton.setOnClickListener {
            saveAndClose(expenditureViewModel)
        }

        expenditureViewModel.availableExpenditures.observe(viewLifecycleOwner) {
            val adapter = AutoCompleteAdapter(this.requireContext(), it.map { e -> e.data })
            binding.expenditureName.setAdapter(adapter)
        }

        return layout
    }

    override fun onEdit() {
        expenditureViewModel.enableEditing()
    }

    override fun getViewModel() = expenditureViewModel
}