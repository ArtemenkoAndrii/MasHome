package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.MessageRuleListRowBinding
import com.mas.mobile.presentation.activity.fragment.MessageRuleListFragment
import com.mas.mobile.presentation.activity.fragment.MessageRuleListFragmentDirections
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.MessageRule

class MessageRuleAdapter(
    private val fragment: MessageRuleListFragment
): BaseAdapter<MessageRule, MessageRuleListRowBinding>(R.layout.message_rule_list_row) {
    override fun bind(binding: MessageRuleListRowBinding, item: MessageRule, prior: MessageRule?) {
        val rowView = binding.messageRuleRowLayout
        binding.messageRule = item
        binding.callback = View.OnClickListener{ viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.standard_row_menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.standard_row_menu_view -> {
                        val action = MessageRuleListFragmentDirections.actionToMessageRules(Action.VIEW.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.standard_row_menu_edit -> {
                        val action = MessageRuleListFragmentDirections.actionToMessageRules(Action.EDIT.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.standard_row_menu_clone -> {
                        val action = MessageRuleListFragmentDirections.actionToMessageRules(Action.CLONE.name, item.id)
                        rowView.findNavController().navigate(action)
                        true
                    }
                    R.id.standard_row_menu_remove -> {
                        fragment.deleteItem(item)
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }
    }

    override fun getBinding(view: View) = MessageRuleListRowBinding.bind(view)
}