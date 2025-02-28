package com.bujnakm.chatapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter for managing authentication fragments (Login and Register) in a ViewPager2.
 *
 * This adapter is used in the authentication screen where users can switch between login and
 * registration using tabs.
 *
 * @param fragmentActivity The parent activity that hosts the ViewPager2.
 */
class AuthPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    /**
     * Returns the number of authentication fragments (Login and Register).
     * In this case, there are only two fragments.
     */
    override fun getItemCount() = 2

    /**
     * Creates and returns the appropriate fragment based on the selected tab position.
     *
     * @param position The position of the fragment in the ViewPager2.
     * @return The corresponding fragment (LoginFragment for position 0, RegisterFragment for position 1).
     */
    override fun createFragment(position: Int): Fragment {
        return if (position == 0) LoginFragment() else RegisterFragment()
    }
}
