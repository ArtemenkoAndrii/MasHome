package com.mas.mobile.presentation.activity.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.CategoryListFragmentBinding
import com.mas.mobile.domain.budget.IconId
import com.mas.mobile.presentation.adapter.CategoryAdapter
import com.mas.mobile.presentation.viewmodel.CategoryListViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class CategoryListFragment : ListFragment() {
    private lateinit var binding: CategoryListFragmentBinding

    val listViewModel: CategoryListViewModel by lazyViewModel {
        this.requireContext().appComponent.categoryListViewModelModel().create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.category_list_fragment, container, false)

        val adapter = CategoryAdapter(this)
        binding = CategoryListFragmentBinding.bind(layout)
        binding.categoryList.adapter = adapter
        binding.categoryList.layoutManager = LinearLayoutManager(requireContext())

        listViewModel.categories.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }

        binding.categoryAddBtn.setOnClickListener {
            val action = resolveAddButtonDestination()
            this.findNavController().navigate(action)
        }

        return layout
    }

    fun getDrawable(id: IconId?): Drawable {
        return this.getIconPack().icons[id?.value]?.drawable
            ?: AppCompatResources.getDrawable(requireContext(), R.drawable.ic_circle)!!
    }

    override fun resolveAddButtonDestination() =
        CategoryListFragmentDirections.actionToCategory(Action.ADD.name)
}

