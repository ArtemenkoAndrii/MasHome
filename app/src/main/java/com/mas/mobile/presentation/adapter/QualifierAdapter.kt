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

            if (item.value.isBlank()) {
                showEditor(binding)
            }

            handleMenuBtn(this, qualifier!!)
            handleSaveBtn(this, qualifier!!, oldQualifier as Qualifier)
            handleCancelBtn(this, qualifier!!)

            qualifierNameViewer.setOnLongClickListener {
                showEditor(binding)
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
                            showEditor(binding)
                            binding.qualifierNameEditor.setOnFocusChangeListener { _, hasFocus ->
                                if (!hasFocus) {
                                    binding.editable = false
                                }
                            }
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

    private fun handleCancelBtn(binding: QualifierListRowBinding, item: Qualifier) {
        binding.qualifierDeleteBtn.setOnClickListener {
            if (item.value.isBlank()) {
                fragment.removeQualifier(item)
            }

            binding.editable = false
        }
    }

    private fun showEditor(binding: QualifierListRowBinding) =
        with(binding) {
            reload()
            editable = true
            qualifierNameEditor.requestFocus()
            qualifierNameEditor.setSelection(qualifierNameEditor.length())

            qualifierNameEditor.post {
                val imm = fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(qualifierNameEditor, InputMethodManager.SHOW_IMPLICIT)
            }
        }

    private fun QualifierListRowBinding.reload() {
        qualifier = oldQualifier?.copy()
    }

    override fun getBinding(view: View) = QualifierListRowBinding.bind(view)
}