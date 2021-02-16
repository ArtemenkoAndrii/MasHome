package com.mas.mobile.presentation.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mas.mobile.R
import java.time.LocalDate
import java.time.LocalTime

class SmsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.expenditure_list_fragment, container, false)


        val now = LocalDate.now()
        val startDate = now.withDayOfMonth(1).atStartOfDay()
        val endDate = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX)

        val reportView: RecyclerView = view.findViewById(R.id.expenditureList)
        reportView.layoutManager = LinearLayoutManager(this.requireContext());
        //reportView.adapter = ExpenditureAdapter()
        //expenseViewModel.getExpenseReport(startDate, endDate).observe(viewLifecycleOwner, Observer { report ->
        //    report.let { (reportView.adapter as ExpenditureAdapter).setReport(it) }
        //})

        return view
    }
}