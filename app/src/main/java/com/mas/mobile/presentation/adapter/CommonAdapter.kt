package com.mas.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

open class CommonAdapter<T>(private val fragmentLayout: Int): RecyclerView.Adapter<CommonAdapter<T>.GroupViewHolder>() {
    private var items: List<T> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonAdapter<T>.GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(fragmentLayout, parent, false) as View
        return GroupViewHolder(view) {item, prior ->
            bind(item, prior, view)
        }
    }

    open fun bind(item: T, prior: T?, rowView: View) {
    }

    override fun onBindViewHolder(holder: CommonAdapter<T>.GroupViewHolder, position: Int) = holder.bind(
        items[position], (if (position > 0) items[position-1] else null)
    )

    override fun getItemCount() = items.size

    inner class GroupViewHolder(private val fragmentRow: View, val processor: (item: T, prior: T?)->Unit): RecyclerView.ViewHolder(fragmentRow) {
        fun bind(item: T, prior: T?) {
            processor(item, prior)
        }
    }

    fun setItems(items: List<T>) {
        val callback = EntityListDiffCallback(this.items, items)
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
        this.items = items
    }
}