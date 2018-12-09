package biko.pougala.coinz

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

// In the Bank Activity, this file is meant to manage the different views that will be displayed based on which tab the user
// clicked on.

class PagerManager(fm: FragmentManager): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? = when(position) {
        0 -> BankFragment.newInstance()
        1 -> ShareFriends.newInstance()
        2 -> Scoreboard.newInstance()
        else -> null
    }

    override fun getPageTitle(position: Int): CharSequence = when(position) {
        0 -> "Bank"
        1 -> "Share coins"
        2 -> "Scoreboards"
        else -> ""
    }

    override fun getCount(): Int = 3
}