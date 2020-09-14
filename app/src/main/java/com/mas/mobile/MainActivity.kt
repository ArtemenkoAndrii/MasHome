package com.mas.mobile

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mas.mobile.adapters.CategoryAdapter
import com.mas.mobile.db.AppDatabase
import com.mas.mobile.db.entity.CategoryEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val errorCategoryLength = "Valid length is 2..25"
    private val errorCategoryDuplication = "Already exist"
    private val valRange = 2..25

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var db: AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this)

        viewManager = LinearLayoutManager(this)
        viewAdapter = CategoryAdapter(db!!)

        recyclerView = findViewById<RecyclerView>(R.id.categoryList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    fun addCategory(view: View) {
        val textView = findViewById<EditText>(R.id.categoryAddEditor)
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
        val textView = findViewById<EditText>(R.id.categoryAddEditor)
        val text = textView.text.toString()

        if (text.length !in valRange) {
            return errorCategoryLength
        } else if (db?.categoryDao()?.findByName(text) != null) {
            return errorCategoryDuplication
        }

        return null
    }

}