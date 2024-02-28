package com.trava

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter


class GeneralFragmentPagerAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {
    private val fragments = ArrayList<Fragment>()
    private val titles = ArrayList<String>()
    private val orderIdList = ArrayList<Int>()

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        titles.add(title)
    }

    fun addFragment(fragment: Fragment, title: String, orderId: Int) {
        fragments.add(fragment)
        orderIdList.add(orderId)
        titles.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence = titles[position]

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE

    }

    fun getFragment(orderId : Int?): Fragment {
        return fragments[orderIdList.indexOf(orderIdList.find { it == orderId }?:0)]
    }

    fun removeFragment(orderId : Int?) {
        val item =orderIdList.find { it == orderId}
        val itemIndex =orderIdList.indexOf(item)
        if(itemIndex != -1) {
            fragments.removeAt(itemIndex)
            orderIdList.removeAt(itemIndex)
        }
    }
}