package com.mas.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mas.mobile.R
import com.mas.mobile.databinding.ExpenditureListRowBinding
import com.mas.mobile.presentation.listener.ListListener
import com.mas.mobile.repository.db.entity.Expenditure

class ExpenditureAdapter(val listener: ListListener<Expenditure>):
    RecyclerView.Adapter<ExpenditureAdapter.ExpenditureViewHolder>() {
    private var expenditure: List<Expenditure> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenditureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expenditure_list_row, parent, false) as View
        return ExpenditureViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenditureViewHolder, position: Int) = holder.bind(
        expenditure[position]
    )

    override fun getItemCount() = this.expenditure.size

    inner class ExpenditureViewHolder(private val fragmentRow: View): RecyclerView.ViewHolder(fragmentRow) {
        fun bind(expenditure: Expenditure) {
            val binding = ExpenditureListRowBinding.bind(fragmentRow)
            binding.expenditure = expenditure

            if (listener.showRowMenu() > 0) {
                binding.callback = View.OnClickListener{ viewMenu ->
                    val menu = PopupMenu(viewMenu.context, viewMenu)
                    menu.inflate(listener.showRowMenu())
                    menu.setOnMenuItemClickListener {
                        listener.onRowMenuClick(it, expenditure)
                    }
                    menu.show()
                }
            } else {
                binding.expenditureRowMenu.visibility = View.GONE
            }

            binding.expenditureRowLayout.setOnClickListener {
                listener.onRowItemClick(it, expenditure)
            }



            /*
            binding.callback = View.OnClickListener{ viewMenu ->
                val menu = PopupMenu(viewMenu.context, viewMenu)
                menu.inflate(R.menu.expenditure_row_menu)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.nav_expenditure_list_row_spend -> {
                            //fragmentRow.findNavController().navigate(R.id.action_expenditure_list_to_spending)
                            true
                        }
                        R.id.nav_expenditure_list_row_report -> {
                            //fragmentRow.findNavController().navigate(R.id.action_expenditure_list_to_spending_list)
                            true
                        }
                        else -> false
                    }
                }
                menu.show()
            }
            */

            /*
            {
                val action = ExpenditureListFragmentDirections
                    .actionToExpenditureSpending(Action.ADD.name, NEW_ITEM, expenditure.id)
                fragmentRow.findNavController().navigate(action)
            }
            */
        }
    }

    fun setExpenditure(expenditure: List<Expenditure>) {
        val callback = EntityListDiffCallback(this.expenditure, expenditure)
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
        this.expenditure = expenditure
    }
}