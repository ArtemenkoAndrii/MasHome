package com.mas.mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mas.mobile.R
import com.mas.mobile.adapters.CategoryAdapter
import com.mas.mobile.db.AppDatabase
import com.mas.mobile.db.entity.CategoryEntity
import kotlinx.android.synthetic.main.category_fragment.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoryFragment: Fragment(), View.OnClickListener {
    private val errorCategoryLength = "Valid length is 2..25"
    private val errorCategoryDuplication = "Already exist"
    private val valRange = 2..25

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var db: AppDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.category_fragment, container, false)
        setUpView(view)
        return view
    }

    private fun setUpView(view: View) {
        db = AppDatabase.getInstance(this.requireContext())

        viewManager = LinearLayoutManager(this.requireContext())
        viewAdapter = CategoryAdapter(db!!)

        val catView: RecyclerView? = view?.findViewById<RecyclerView>(R.id.categoryList)

        recyclerView = catView?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }!!

        view?.categoryAddButton.setOnClickListener(this)
    }

    fun addCategory(view: View) {
        val textView = view?.findViewById<EditText>(R.id.categoryAddEditor)
        val error = validate(view)

        error?.let {
            textView.requestFocus()
            textView.error = error
        } ?: run {
            val category = CategoryEntity(name = textView.text.toString())
            GlobalScope.launch {
                db?.categoryDao()?.insertAll(category)
            }
            val size = db?.categoryDao()?.getAll()?.size
            viewAdapter.notifyItemInserted(size!!)
            recyclerView.scrollToPosition(size!!)
            textView.text.clear()
        }
    }

    private fun validate(view: View): String? {
        val textView = view?.findViewById<EditText>(R.id.categoryAddEditor)
        val text = textView.text.toString()

        if (text.length !in valRange) {
            return errorCategoryLength
        } else if (db?.categoryDao()?.findByName(text) != null) {
            return errorCategoryDuplication
        }

        return null
    }

    override fun onClick(v: View?) {
        this.view?.let { addCategory(it) }
    }
}