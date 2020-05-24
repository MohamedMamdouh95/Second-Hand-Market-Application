package com.example.lab2

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.search_dialog.*

class FilterDialogFragment(private val myContract: MyContract) : DialogFragment() {
    constructor() : this(object : MyContract {
        override fun searchCriteria(
            minValue: Int,
            maxValue: Int,
            location: String,
            category: String
        ) {
        }

    })

    private var dialogView: View? = null
    private val TAG = "DIALOG_FRAGMENT"


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.search_dialog, null)
        return AlertDialog.Builder(context).setView(dialogView)
            // Add action buttons
            .setPositiveButton(
                R.string.search
            ) { dialog, _ ->
                //user clicked on search
                //get price
                val minValue = textMin.text.toString().toInt()
                val maxValue = textMax.text.toString().toInt()
                //get location
                val location = searchDialogLocationEditText.editText?.text.toString()
                val category = searchDialogCategory.editText?.text.toString()
                myContract.searchCriteria(minValue, maxValue, location, category)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dialog?.cancel()
            }.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchDialogSeekBar.setOnRangeSeekbarChangeListener { minValue, maxValue ->
            textMin.text = minValue.toString()
            textMax.text = maxValue.toString()
        }
        val categories = resources.getStringArray(R.array.Categories)
        val categoriesAdapter =
            ArrayAdapter(requireContext(), R.layout.category_list_item, categories)

        val keyboardHiderListener = View.OnFocusChangeListener { innerView: View?, hasFocus ->
            if (hasFocus) {
                val inputMethodService =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodService.hideSoftInputFromWindow(innerView?.windowToken, 0)
            }
        }

        (searchDialogCategory.editText as? AutoCompleteTextView)?.onFocusChangeListener =
            keyboardHiderListener
        (searchDialogCategory.editText as? AutoCompleteTextView)?.setAdapter(categoriesAdapter)


    }

    override fun onDestroyView() {
        dialogView = null
        super.onDestroyView()
    }

}