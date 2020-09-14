package com.mas.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mas.mobile.R
import com.mas.mobile.db.AppDatabase
import com.mas.mobile.db.entity.CategoryEntity
import kotlinx.android.synthetic.main.category_row.view.*

class CategoryAdapter(private val dataset: AppDatabase) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryAdapter.CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_row, parent, false) as View
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categories = dataset.categoryDao().getAll()
        holder.view.categoryName.text = categories[position]?.name

        holder.view.categoryRemoveButton.setOnClickListener {
            dataset.categoryDao().delete(categories[position])
            notifyDataSetChanged();
        }
    }

    override fun getItemCount() = dataset.categoryDao().getAll().size
}