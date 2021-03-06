package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.mas.mobile.R
import com.mas.mobile.databinding.SpendingFragmentBinding
import com.mas.mobile.presentation.adapter.AutoCompleteAdapter
import com.mas.mobile.presentation.viewmodel.SpendingViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Searchable

class SpendingFragment: CommonFragment() {
    private lateinit var binding: SpendingFragmentBinding
    private val args: SpendingFragmentArgs by navArgs()
    private val spendingViewModel: SpendingViewModel by viewModels {
        createWithFactory {
            SpendingViewModel(requireActivity().application,
                args.spendingId,
                args.expenditureId,
                args.budgetId ,
                action != Action.VIEW
            )
        }
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
            handleSaveAndClose(spendingViewModel)
        }

        binding.spendingDate.setOnClickListener {
            showDateTimeDialog(binding.spendingDate)
        }

        with(binding.spendingExpenditure) {
            this.setOnItemClickListener { _, view, _, _ ->
                handleExplicitExpenditureChoice(view)
            }
            this.setOnDismissListener {
                handleImpliedExpenditureChoice(this)
            }
        }

        spendingViewModel.availableExpenditures.observe(viewLifecycleOwner, {
            val adapter = AutoCompleteAdapter(this.requireContext(), it.map { e -> e.data } )
            binding.spendingExpenditure.setAdapter(adapter)
        })

        return layout
    }

    private fun handleExplicitExpenditureChoice(view: View) {
        val chosenItem = view.findViewById<TextView>(R.id.autocompleteName)
        val expenditureId = (chosenItem.tag ?: 0) as Int
        spendingViewModel.expenditureId.value = expenditureId
    }

    private fun handleImpliedExpenditureChoice(view: MaterialAutoCompleteTextView) {
        if (view.adapter.count == SINGLE_SUGGESTION) {
            val item = view.adapter.getItem(0) as Searchable
            if (item.name == view.text.toString()) {
                spendingViewModel.expenditureId.value = item.id
            }
        }
    }

    companion object {
        const val SINGLE_SUGGESTION = 1
    }

}
