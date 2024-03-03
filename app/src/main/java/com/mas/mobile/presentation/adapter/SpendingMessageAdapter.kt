package com.mas.mobile.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import com.mas.mobile.R
import com.mas.mobile.databinding.MessageListRowBinding
import com.mas.mobile.domain.message.Message
import com.mas.mobile.presentation.activity.fragment.MessageListFragment
import com.mas.mobile.presentation.activity.fragment.MessageListFragmentDirections
import com.mas.mobile.presentation.viewmodel.validator.Action


class SpendingMessageAdapter(
    private val fragment: MessageListFragment
): BaseAdapter<Message, MessageListRowBinding>(R.layout.message_list_row) {

    override fun bind(binding: MessageListRowBinding, item: Message, prior: Message?) {
        binding.message = item

        mapChipButton(item, binding)

        binding.expenditureCallback = View.OnClickListener { goToSpending(item) }
        binding.messageListRowLayout.setOnClickListener { goToSpending(item) }

        binding.callback = View.OnClickListener { viewMenu ->
            val menu = PopupMenu(viewMenu.context, viewMenu)
            menu.inflate(R.menu.message_row_menu)
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.message_row_menu_spending -> {
                        goToSpending(item)
                    }
                    R.id.message_row_menu_remove ->
                        with(fragment) {
                            showConfirmationDialog(getResourceService().messageAreYouSure()) {
                                listViewModel.remove(item)
                            }
                        }
                    else -> {}
                }
                true
            }
            menu.show()
        }
    }

    private fun goToSpending(item: Message) {
        fragment.listViewModel.markAsRead(item)

        if (item.spendingId != null) {
            MessageListFragmentDirections.actionToExistingSpending(Action.VIEW.name, item.spendingId!!.value, item.id.value)
        } else {
            MessageListFragmentDirections.actionToNewSpending(Action.ADD.name, item.id.value)
        }.also {
            fragment.findNavController().navigate(it)
        }
    }

    private fun mapChipButton(item: Message, binding:MessageListRowBinding) {
        with(binding.messageListRowSuggestedExpenditure) {
            if (item.hasSpending()) {
                this.setChipBackgroundColorResource(R.color.colorGray)
                val expenditure = fragment.listViewModel.getSpending(item.spendingId!!)?.expenditure
                this.setText(expenditure?.name ?: NA)
            } else {
                when (item.status) {
                    is Message.Matched -> {
                        this.setChipBackgroundColorResource(R.color.colorAccent)
                        this.setText(fragment.getResourceService().spendingMessageClickToBind())
                    }
                    is Message.Recommended -> {
                        this.setChipBackgroundColorResource(R.color.colorYellow)
                        this.setText(fragment.getResourceService().spendingMessageClickToDiscover())
                    }
                    else -> {}
                }
            }
        }
    }

    override fun getBinding(view: View) = MessageListRowBinding.bind(view)

    private companion object {
        const val NA = "N/A"
    }
}