package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.SpendingFragmentBinding
import com.mas.mobile.presentation.adapter.AutoCompleteAdapter
import com.mas.mobile.presentation.viewmodel.SpendingViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class SpendingFragment : BaseFragment<SpendingViewModel>() {
    private lateinit var binding: SpendingFragmentBinding
    private val args: SpendingFragmentArgs by navArgs()

    private val spendingViewModel: SpendingViewModel by lazyViewModel {
        this.requireContext().appComponent.spendingViewModel().create(
            args.spendingId,
            args.expenditureId,
            args.budgetId,
            args.envelop,
            action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.spending_fragment, container, false)

        action = Action.valueOf(args.action)

        binding = SpendingFragmentBinding.bind(layout)
        binding.model = spendingViewModel
        binding.lifecycleOwner = this

        binding.spendingSaveButton.setOnClickListener {
            saveAndClose(spendingViewModel)
        }

        binding.spendingDate.setOnClickListener {
            showDateTimeDialog(binding.spendingDate)
        }

        spendingViewModel.availableExpenditures.observe(viewLifecycleOwner) {
            val adapter = AutoCompleteAdapter(this.requireContext(), it.map { e -> e.data })
            binding.spendingExpenditure.setAdapter(adapter)
        }

        return layout
    }

    override fun getViewModel() = spendingViewModel
}
