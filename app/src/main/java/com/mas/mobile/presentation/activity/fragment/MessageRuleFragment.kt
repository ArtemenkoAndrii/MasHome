package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.MessageRuleFragmentBinding
import com.mas.mobile.presentation.activity.converter.TextDrawable
import com.mas.mobile.presentation.viewmodel.MessageRuleViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.Searchable

class MessageRuleFragment : ItemFragment<MessageRuleViewModel>() {
    private val args: MessageRuleFragmentArgs by navArgs()
    private lateinit var binding: MessageRuleFragmentBinding

    override val viewModel: MessageRuleViewModel by lazyViewModel {
        this.requireContext().appComponent.messageRuleViewModel()
            .create(args.messageRuleId, action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.message_rule_fragment, container, false)

        action = Action.valueOf(args.action)

        binding = MessageRuleFragmentBinding.bind(layout)
        binding.model = viewModel
        binding.lifecycleOwner = this

        binding.messageRuleSaveButton.setOnClickListener {
            saveAndClose(viewModel)
        }

        ArrayAdapter(this.requireContext(),
            R.layout.autocomplete_item,
            viewModel.availableExpenditures.toList()).also {
            binding.mssageRuleExpenditure.setAdapter(it)
        }

//        messageRuleViewModel.availableExpenditures.observe(viewLifecycleOwner) {
//            val adapter = AutoCompleteAdapter(this.requireContext(), it.map { e -> e.data })
//            binding.mssageRuleExpenditure.setAdapter(adapter)
//        }

        with(binding.mssageRuleExpenditure) {
            this.setOnItemClickListener { _, view, _, _ ->
                handleExplicitExpenditureChoice(view)
            }
            this.setOnDismissListener {
                handleImpliedExpenditureChoice(this)
            }
        }

        with(binding.messageRuleCurrencyLayout) {
            isEndIconVisible = true
            viewModel.currency.observeForever {
                endIconDrawable = TextDrawable(requireContext(), it.symbol)
            }

            val onClick = View.OnClickListener {
                showCurrencyPicker { currency ->
                    viewModel.currency.value = currency
                    clearFocus()
                }
            }

            editText?.setOnClickListener(onClick)
            setEndIconOnClickListener(onClick)
        }

        return layout
    }

    private fun handleExplicitExpenditureChoice(view: View) {
        val chosenItem = view.findViewById<TextView>(R.id.autocompleteName)
        val expenditureId = (chosenItem.tag ?: 0) as Int
        viewModel.expenditureId = expenditureId
    }

    private fun handleImpliedExpenditureChoice(view: MaterialAutoCompleteTextView) {
        if (view.adapter.count == SINGLE_SUGGESTION) {
            val item = view.adapter.getItem(0) as Searchable
            if (item.name == view.text.toString()) {
                viewModel.expenditureId = item.id
            }
        }
    }

    companion object {
        const val SINGLE_SUGGESTION = 1
    }
}
