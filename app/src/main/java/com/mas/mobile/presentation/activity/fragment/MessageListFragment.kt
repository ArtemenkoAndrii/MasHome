package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.MessageListFragmentBinding
import com.mas.mobile.presentation.adapter.SpendingMessageAdapter
import com.mas.mobile.presentation.viewmodel.MessageListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.repository.db.entity.SpendingMessage

class MessageListFragment: BaseListFragment() {
    private lateinit var binding: MessageListFragmentBinding

    private val messageListViewModel: MessageListViewModel by lazyViewModel {
        this.requireContext().appComponent.messageListViewModel().create("test")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.message_list_fragment, container, false)

        val adapter = SpendingMessageAdapter(this)
        binding = MessageListFragmentBinding.bind(layout)
        binding.messageList.adapter = adapter
        binding.messageList.layoutManager = LinearLayoutManager(this.requireContext())

        messageListViewModel.messages.observe(viewLifecycleOwner) { message ->
            adapter.setItems(message)
        }

        binding.messageAddBtn.visibility = View.GONE

        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.spending_message_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.spending_message_list_menu_rules -> {
                val action = resolveAddButtonDestination()
                this.findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun resolveAddButtonDestination() =
        MessageListFragmentDirections.actionToMessageRules(Action.VIEW.name)

    fun removeItem(item: SpendingMessage) {
        showConfirmationDialog {
            messageListViewModel.remove(item)
        }
    }

    fun markAsRead(item: SpendingMessage) {
        messageListViewModel.markAsRead(item)
    }
}