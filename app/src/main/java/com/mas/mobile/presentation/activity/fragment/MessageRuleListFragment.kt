package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.MessageRuleListFragmentBinding
import com.mas.mobile.presentation.adapter.MessageRuleAdapter
import com.mas.mobile.presentation.viewmodel.MessageRuleListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

open class MessageRuleListFragment : ListFragment() {
    private val args: SpendingListFragmentArgs by navArgs()
    private lateinit var binding: MessageRuleListFragmentBinding

    val listViewModel: MessageRuleListViewModel by lazyViewModel {
        this.requireContext().appComponent.messageRuleListViewModel().create("test")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.message_rule_list_fragment, container, false)

        val adapter = MessageRuleAdapter(this)
        binding = MessageRuleListFragmentBinding.bind(layout)
        binding.messageRuleList.adapter = adapter
        binding.messageRuleList.layoutManager = LinearLayoutManager(this.requireContext())

        listViewModel.messageRules.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }

        binding.messageRuleAddBtn.setOnClickListener {
            val action = resolveAddButtonDestination()
            this.findNavController().navigate(action)
        }

        return layout
    }

    override fun resolveAddButtonDestination() =
        MessageRuleListFragmentDirections.actionToMessageRules(Action.ADD.name)
}

