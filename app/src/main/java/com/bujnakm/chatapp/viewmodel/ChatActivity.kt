package com.bujnakm.chatapp.viewmodel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bujnakm.chatapp.R
import com.bujnakm.chatapp.NewChatFragment
import com.bujnakm.chatapp.TabsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * ChatActivity serves as the main screen after login.
 * It manages a tabbed layout containing the "Chats" and "Profile" sections.
 */
class ChatActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created.
     * Sets up the main layout and initializes the tab system.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tabs) // Load the Chat/Profile layout

        setupTabs() // Initialize the tab layout
    }

    /**
     * Sets up the tab layout with a ViewPager2 and TabLayout.
     * This allows users to switch between "Chats" and "Profile" tabs.
     */
    private fun setupTabs() {
        val viewPager: ViewPager2 = findViewById(R.id.view_pager_tabs)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout_tabs)

        // Attach the ViewPager2 to the adapter
        viewPager.adapter = TabsPagerAdapter(this)

        // Link the TabLayout with the ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Chats" // Chat List
                1 -> "Profile" // Profile
                else -> null
            }
        }.attach()
    }

    /**
     * Opens the NewChatFragment as a dialog when the user wants to start a new chat.
     */
     fun openNewChatFragment() {
        val newChatFragment = NewChatFragment()
        newChatFragment.show(supportFragmentManager, "NewChatFragment")
    }

}
