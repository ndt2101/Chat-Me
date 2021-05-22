package com.tuan2101.chatme.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tuan2101.chatme.fragment.ChatFragment
import com.tuan2101.chatme.fragment.ProfileFragment
import com.tuan2101.chatme.fragment.GroupChatFragment

class TabSwitcher(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            0-> {
                var chatFragment = ChatFragment()
                return chatFragment
            }
            1-> {
                var groupChatFragment  = GroupChatFragment()
                return groupChatFragment
            }
            2-> {
                var contactFragment = ProfileFragment()
                return contactFragment
            }

            else -> {
                return null!!
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
            0-> {
                return "Chat"
            }
            1-> {
                return "Group Chat"
            }
            2-> {
                return "Profile"
            }

            else -> {
                return null!!
            }
        }
    }

}