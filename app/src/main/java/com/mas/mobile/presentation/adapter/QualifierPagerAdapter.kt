package com.mas.mobile.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mas.mobile.presentation.activity.fragment.QualifierTabFragment

class QualifierPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
         when (position) {
            0 -> QualifierTabFragment(false)
            1 -> QualifierTabFragment(true)
            else -> throw IllegalArgumentException("Invalid tab position")
    }
}