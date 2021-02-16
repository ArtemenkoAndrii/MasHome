package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.MessageListRowBinding
import com.mas.mobile.presentation.activity.fragment.SpendingListFragmentDirections
import com.mas.mobile.presentation.viewmodel.MessageListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.SpendingMessage

class SpendingMessageAdapter(
    private val messageListViewModel: MessageListViewModel
): BaseAdapter<SpendingMessage, MessageListRowBinding>(R.layout.message_list_row) {

    override fun bind(binding:MessageListRowBinding, item: SpendingMessage, prior: SpendingMessage?) {
        binding.message = item
        binding.callback = View.OnClickListener{ viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.message_row_menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.message_row_menu_spending -> {
                        // item.spendingId
                        val action = SpendingListFragmentDirections.actionToSpending(Action.EDIT.name, item.spendingId!!)
                        binding.messageListRowLayout.findNavController().navigate(action)
                        true
                    }
                    R.id.message_row_menu_remove -> {
                        messageListViewModel.remove(item)
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }
    }

    override fun getBinding(view: View) = MessageListRowBinding.bind(view)
}