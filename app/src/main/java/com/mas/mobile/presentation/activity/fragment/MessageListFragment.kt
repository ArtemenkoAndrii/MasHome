package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.NavGraphDirections
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.MessageListFragmentBinding
import com.mas.mobile.presentation.adapter.SpendingMessageAdapter
import com.mas.mobile.presentation.viewmodel.MessageListViewModel

class MessageListFragment: ListFragment() {
    private lateinit var binding: MessageListFragmentBinding

    val listViewModel: MessageListViewModel by lazyViewModel {
        this.requireContext().appComponent.messageListViewModel().create("dummy")
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

        listViewModel.messages.observe(viewLifecycleOwner) { message ->
            adapter.setItems(message)
        }

        binding.messageAddBtn.visibility = View.GONE
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.spending_message_list_menu_template -> go(MessageListFragmentDirections.actionToMessageTemplates())
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.message_list_menu, menu)
    }

    private fun showDialog() {
        if (listViewModel.suggestEnableCapturing()) {
            showConfirmationDialog(getResourceService().messageCapturingDisabled()) {
                this.findNavController().navigate(NavGraphDirections.actionToSettings(true))
            }
        }
    }

    override fun resolveAddButtonDestination(): NavDirections {
        TODO("Not yet implemented")
    }
}