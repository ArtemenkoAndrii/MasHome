package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.MessageListRowBinding
import com.mas.mobile.model.SpendingMessageEnvelop
import com.mas.mobile.presentation.activity.fragment.MessageListFragment
import com.mas.mobile.presentation.activity.fragment.MessageListFragmentDirections
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.SpendingMessage

class SpendingMessageAdapter(
    private val fragment: MessageListFragment
): BaseAdapter<SpendingMessage, MessageListRowBinding>(R.layout.message_list_row) {

    override fun bind(binding:MessageListRowBinding, item: SpendingMessage, prior: SpendingMessage?) {
        binding.message = item

        val expenditureName = prepareExpenditureName(item) {
            binding.messageListRowSuggestedExpenditure.setChipBackgroundColorResource(R.color.colorAccent)
        }
        binding.messageListRowSuggestedExpenditure.setText(expenditureName)

        binding.expenditureCallback = View.OnClickListener { goToSpending(item) }

        binding.callback = View.OnClickListener { viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.message_row_menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.message_row_menu_spending -> {
                        goToSpending(item)
                    }
                    R.id.message_row_menu_remove -> {
                        fragment.removeItem(item)
                    }
                    else -> {}
                }
                true
            }
            menu.show()
        }
    }

    private fun goToSpending(item: SpendingMessage) {
        fragment.markAsRead(item)

        val envelop = SpendingMessageEnvelop(item.id, item.suggestedAmount, item.message)
        if (item.spendingId != null) {
            MessageListFragmentDirections.actionToExistingSpending(Action.VIEW.name, item.spendingId!!, envelop.toString())
        } else {
            MessageListFragmentDirections.actionToNewSpending(Action.ADD.name, envelop.toString())
        }.also {
            fragment.findNavController().navigate(it)
        }
    }

    private fun prepareExpenditureName(item: SpendingMessage, styleProcessor: () -> Unit): String {
        return if (item.suggestedExpenditureName.isNullOrEmpty()) {
            styleProcessor()
            fragment.getResourceService().spendingMessageClickToAdd()
        } else {
            item.suggestedExpenditureName!!
        }
    }

    override fun getBinding(view: View) = MessageListRowBinding.bind(view)
}