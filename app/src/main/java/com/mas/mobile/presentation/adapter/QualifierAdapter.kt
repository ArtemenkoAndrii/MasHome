package com.mas.mobile.presentation.adapter

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.PopupMenu
import com.mas.mobile.R
import com.mas.mobile.databinding.QualifierListRowBinding
import com.mas.mobile.domain.message.Qualifier
import com.mas.mobile.presentation.activity.fragment.QualifierTabFragment


class QualifierAdapter(val fragment: QualifierTabFragment): BaseAdapter<Qualifier, QualifierListRowBinding>(R.layout.qualifier_list_row)
{
    override fun bind(binding: QualifierListRowBinding, item: Qualifier, prior: Qualifier?) {
        with(binding) {
            qualifier = item
            oldQualifier = item.copy()

            qualifier.let {
                if (it?.value == "") {
                    binding.editable = true
                    binding.qualifierNameEditor.requestFocus()
                    binding.qualifierNameEditor.setSelection(0)

                    binding.qualifierNameEditor.post {
                        val imm = fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.qualifierNameEditor, InputMethodManager.SHOW_IMPLICIT)
                    }
                }
            }

            handleMenuBtn(this, item)
            handleSaveBtn(this, item, oldQualifier as Qualifier)
            handleDeleteBtn(this, item)

            qualifierNameViewer.setOnLongClickListener {
                editable = true
                true
            }
        }
    }

    private fun handleMenuBtn(binding: QualifierListRowBinding, item: Qualifier) {
        binding.qualifierMenuBtn.setOnClickListener {
            with(PopupMenu(it.context, it)) {
                inflate(R.menu.ud_row_menu)
                setOnMenuItemClickListener { action ->
                    when (action.itemId) {
                        R.id.ud_row_menu_edit -> {
                            binding.editable = true
                            true
                        }
                        else -> {
                            fragment.removeQualifier(item)
                            true
                        }
                    }
                }
                show()
            }
        }
    }

    private fun handleSaveBtn(binding: QualifierListRowBinding, item: Qualifier, oldValue: Qualifier) {
        binding.qualifierSaveBtn.setOnClickListener {
            fragment.removeQualifier(oldValue)
            fragment.saveQualifier(item)
            binding.editable = false
        }
    }

    private fun handleDeleteBtn(binding: QualifierListRowBinding, item: Qualifier) {
        binding.qualifierDeleteBtn.setOnClickListener {
            fragment.removeQualifier(item)
            binding.editable = false
        }
    }

    override fun getBinding(view: View) = QualifierListRowBinding.bind(view)
}