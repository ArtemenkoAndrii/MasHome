package com.mas.mobile.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.mas.mobile.R
import com.mas.mobile.repository.db.entity.Searchable
import java.util.*

class AutoCompleteAdapter<T: Searchable>(
    ctx: Context,
    private val items: List<T>
) : ArrayAdapter<T>(ctx, R.layout.autocomplete_item, items) {

    private val allSuggestions: List<T> = items.toMutableList()
    private val filter: Filter = PersistableFilter()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.autocomplete_item, parent, false)
        val suggestion = this.items[position]

        val name = layout.findViewById(R.id.autocompleteName) as TextView
        name.text = suggestion.name
        name.tag = suggestion.id

        return layout
    }

    override fun getFilter(): Filter {
        return this.filter
    }

    @Suppress("UNCHECKED_CAST")
    inner class PersistableFilter : Filter() {
        override fun convertResultToString(resultValue: Any): CharSequence {
            return StringBuilder().append((resultValue as T).name)
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()

            val term = constraint?.toString()?.lowercase(Locale.ROOT)
            val suggestions = if (term != null) {
                allSuggestions.filter { c -> c.name.lowercase(Locale.ROOT).contains(term) }
            } else {
                allSuggestions.toMutableList()
            }

            result.values = suggestions
            result.count = suggestions.size

            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if ((results?.count ?: 0) > 0) {
                clear()
                addAll(results?.values as List<T>)
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

}