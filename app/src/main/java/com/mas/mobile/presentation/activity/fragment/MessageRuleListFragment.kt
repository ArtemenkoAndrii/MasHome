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
import com.mas.mobile.repository.db.entity.MessageRule

open class MessageRuleListFragment : BaseListFragment() {
    private val args: SpendingListFragmentArgs by navArgs()
    private lateinit var binding: MessageRuleListFragmentBinding

    private val messageRuleListViewModel: MessageRuleListViewModel by lazyViewModel {
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

        messageRuleListViewModel.messageRules.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }

        binding.messageRuleAddBtn.setOnClickListener {
            val action = resolveAddButtonDestination()
            this.findNavController().navigate(action)
        }

        hideBottomMenu()
        return layout
    }

    override fun resolveAddButtonDestination() =
        MessageRuleListFragmentDirections.actionToMessageRules(Action.ADD.name)

    override fun onDestroy() {
        showBottomMenu()
        super.onDestroy()
    }

    fun deleteItem(item: MessageRule) {
        showConfirmationDialog {
            messageRuleListViewModel.remove(item)
        }
    }
}

