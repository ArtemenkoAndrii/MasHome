package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mas.mobile.R
import com.mas.mobile.databinding.ExpenditureFragmentBinding
import com.mas.mobile.presentation.adapter.AutoCompleteAdapter
import com.mas.mobile.presentation.viewmodel.ExpenditureViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class ExpenditureFragment: CommonFragment() {
    private lateinit var binding: ExpenditureFragmentBinding
    private val args: ExpenditureFragmentArgs by navArgs()
    private val expenditureViewModel: ExpenditureViewModel by viewModels {
        createWithFactory {
            ExpenditureViewModel(requireActivity().application,
                args.expenditureId,
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
        val layout = inflater.inflate(R.layout.expenditure_fragment, container, false)

        action = Action.valueOf(args.action)

        binding = ExpenditureFragmentBinding.bind(layout)
        binding.expenditure = expenditureViewModel
        binding.lifecycleOwner = this

        binding.expenditureSaveButton.setOnClickListener {
            handleSaveAndClose(expenditureViewModel)
        }

        expenditureViewModel.availableExpenditures.observe(viewLifecycleOwner, {
            val adapter = AutoCompleteAdapter(this.requireContext(), it.map { e -> e.data } )
            binding.expenditureName.setAdapter(adapter)
        })

        return layout
    }

    override fun onEdit() {
        expenditureViewModel.enableEditing()
    }

    override fun onClone() {
        expenditureViewModel.enableEditing()
    }

    override fun onRemove() {
        expenditureViewModel.save(Action.REMOVE)
    }

    private fun handleSaveAndClose() {
        if (expenditureViewModel.save(action)) {
            findNavController().popBackStack()
        }
    }
}