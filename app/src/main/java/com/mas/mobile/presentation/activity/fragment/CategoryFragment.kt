package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.databinding.CategoryFragmentBinding
import com.mas.mobile.presentation.viewmodel.CategoryViewModel
import com.mas.mobile.presentation.viewmodel.validator.Action

class CategoryFragment: ItemFragment<CategoryViewModel>() {
    private val args: CategoryFragmentArgs by navArgs()
    private lateinit var binding: CategoryFragmentBinding

    override val viewModel: CategoryViewModel by lazyViewModel {
        this.requireContext().appComponent.categoryViewModelModel()
            .create(args.categoryId, action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val layout = inflater.inflate(R.layout.category_fragment, container, false)

        action = Action.valueOf(args.action)

        binding = CategoryFragmentBinding.bind(layout)
        binding.model = viewModel
        binding.lifecycleOwner = this

        viewModel.merchants.observe(viewLifecycleOwner) { merchants ->
            binding.merchantChipGroup.removeAllViews()
            merchants.forEach { merchant ->
                val chip = Chip(context)
                chip.text = merchant
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    viewModel.removeMerchant(merchant)
                }
                binding.merchantChipGroup.addView(chip)
            }
        }
        binding.addChipButton.setOnClickListener {
            viewModel.addMerchant()
        }

        binding.categorySaveButton.setOnClickListener {
            saveAndClose(viewModel)
        }

        return layout
    }
}