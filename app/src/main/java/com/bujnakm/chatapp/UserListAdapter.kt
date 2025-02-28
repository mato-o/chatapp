package com.bujnakm.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bujnakm.chatapp.model.User

// Adapter for displaying a list of users in a RecyclerView
class UserListAdapter(
    private val users: List<User>, // List of users to display
    private val onUserClick: (User) -> Unit // Callback function triggered when a user is clicked
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    // ViewHolder class to represent invdividual user items in the list
    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUsername: TextView = view.findViewById(R.id.tvUsername) // Reference to username TextView

        // Binds the user data to the view
        fun bind(user: User) {
            tvUsername.text = user.username // Set the username text
            itemView.setOnClickListener { onUserClick(user) } // Set click listener to trigger the callback function
        }
    }

    // Creates new ViewHolder instances when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    // Binds data to the ViewHolder at a specific position
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = users.size
}
