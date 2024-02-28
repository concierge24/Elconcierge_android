package com.trava.user.walkthrough

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class WalkthroughAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        val frag = WalkthroughFragment()
        val bundle = Bundle()
        bundle.putInt("pos",position)
        frag.arguments = bundle
        return frag

    }

    override fun getCount(): Int {
        return 3
    }
}