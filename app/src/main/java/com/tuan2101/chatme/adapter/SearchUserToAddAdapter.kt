package com.tuan2101.chatme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.SearchViewHolderBinding
import com.tuan2101.chatme.viewModel.User
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.MutableLiveData
import com.tuan2101.chatme.viewModel.hideKeyboard

class SearchUserToAddAdapter (
    val context: Context,
    val users: List<User>,
    var groupMembers: MutableLiveData<List<User>>,
    var groupMembersClone: List<User>
) : RecyclerView.Adapter<SearchUserToAddAdapter.ViewHolder?>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var user = users[position]
        holder.binding.userName.text = user.getName()
        Picasso.get()
            .load(user.getAvatar())
            .fit()
            .into(holder.binding.imageProfile)

                for (i in groupMembersClone) {
                    if (i.getUid() == user.getUid()) {
                        holder.clicked = true
                        holder.itemView.setBackgroundResource(R.drawable.edittext_style)
                        break
                    } else {
                        holder.clicked = false
                        holder.itemView.setBackgroundResource(R.color.white)
                    }
                }
        holder.binding.userSearched.setOnClickListener {
            context.hideKeyboard(holder.itemView)
            if (!holder.clicked) {
                holder.itemView.setBackgroundResource(R.drawable.edittext_style)
                (groupMembersClone as ArrayList<User>).add(user)
                groupMembers.postValue(groupMembersClone)
                holder.clicked = true
            }
            else {
                holder.itemView.setBackgroundResource(R.color.white)

                for (i in groupMembersClone.indices) {
                    if (groupMembersClone[i].getUid() == user.getUid()) {
                        user = groupMembersClone[i]
                    }
                }
                (groupMembersClone as ArrayList<User>).remove(user)
                groupMembers.postValue(groupMembersClone)
                holder.clicked = false
            }


            groupMembers.value?.let { it1 -> it1.forEach {
                println(it.getUid()) }}

        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class ViewHolder(val binding: SearchViewHolderBinding, var clicked: Boolean) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SearchViewHolderBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, false)
            }
        }
    }
}
