package com.example.p

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.p.Fragments.Fragment1
import com.example.p.Fragments.Fragment2
import com.example.p.Fragments.Fragment3
import com.example.p.Fragments.Fragment4

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 4 // 총 3개의 페이지
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Fragment1()
            1 -> Fragment2()
            2 -> Fragment3()
            3 -> Fragment4()
            else -> Fragment1()
        }
    }
}