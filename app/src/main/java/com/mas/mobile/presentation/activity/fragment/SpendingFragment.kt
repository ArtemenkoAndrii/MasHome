package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.navArgs
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.SpendingFragmentBinding
import com.mas.mobile.presentation.viewmodel.SpendingViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class SpendingFragment : ItemFragment<SpendingViewModel>() {
    private lateinit var binding: SpendingFragmentBinding
    private val args: SpendingFragmentArgs by navArgs()

    override val viewModel: SpendingViewModel by lazyViewModel {
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
        binding.model = viewModel
        binding.lifecycleOwner = this

        binding.spendingSaveButton.setOnClickListener {
            saveAndClose(viewModel)
        }

        binding.spendingDate.setOnClickListener {
            showDateTimeDialog(binding.spendingDate)
        }


        ArrayAdapter(this.requireContext(),
            R.layout.autocomplete_item,
            viewModel.availableExpenditures.toList()).also {
            binding.spendingExpenditure.setAdapter(it)
        }

//        viewModel.availableExpenditures.observe(viewLifecycleOwner) {
//            val adapter = AutoCompleteAdapter(this.requireContext(), it.map { e -> e.data })
//            binding.spendingExpenditure.setAdapter(adapter)
//        }

        return layout
    }
}
