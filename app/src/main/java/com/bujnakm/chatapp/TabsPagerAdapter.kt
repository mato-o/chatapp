package com.bujnakm.chatapp


import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

// Adapter for managing fragments in a ViewPager2 (Tab navigation)
class TabsPagerAdapter(activity: androidx.fragment.app.FragmentActivity) : FragmentStateAdapter(activity) {

    // Defines the number of tabs in the ViewPager
    override fun getItemCount(): Int = 2  // Two tabs: Chats and Profile

    // Creates the appropriate fragment based on the selected tab position
    override fun createFragment(position: Int): Fragment {
        Log.d("TabsPagerAdapter", "Creating fragment for position: $position")
        return when (position) {
            0 -> {
                Log.d("TabsPagerAdapter", "Creating ChatListFragment")
                ChatListFragment()  // Chats Tab
            }
            1 -> {
                Log.d("TabsPagerAdapter", "Creating ProfileFragment")
                ProfileFragment()   // Profile Tab
            }
            else -> throw IllegalStateException("Unexpected position: $position") // Ensures no invalid tab positions
        }
    }
}
