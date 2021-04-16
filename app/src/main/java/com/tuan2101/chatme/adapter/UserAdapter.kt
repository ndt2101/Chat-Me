package com.tuan2101.chatme.adapter

import android.content.Context
import android.view.LayoutInflater

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

import com.tuan2101.chatme.databinding.SearchViewHolderBinding

import com.tuan2101.chatme.viewModel.User

class UserAdapter(
    val context: Context,
    val users: List<User>,
    val isChatCheck: Boolean
) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = users[position]
        holder.binding.userName.text = user.getName()
        Picasso.get()
            .load(user.getAvatar())
            .fit()
            .into(holder.binding.imageProfile)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class ViewHolder(val binding: SearchViewHolderBinding ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SearchViewHolderBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}