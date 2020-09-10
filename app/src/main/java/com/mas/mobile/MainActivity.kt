package com.mas.mobile

import kotlinx.coroutines.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mas.mobile.adapters.CategoryAdapter
import com.mas.mobile.db.AppDatabase
import com.mas.mobile.db.entity.CategoryEntity

class MainActivity : AppCompatActivity() {
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
        val editText = findViewById<EditText>(R.id.categoryAddEditor)

        val category = CategoryEntity(firstName = editText.text.toString())
        GlobalScope.launch {
            db?.categoryDao()?.insertAll(category)
        }

        val size = db?.categoryDao()?.getAll()?.size
        viewAdapter.notifyItemInserted(size!!)
        recyclerView.scrollToPosition(size!!)
        editText.text.clear()
    }

}