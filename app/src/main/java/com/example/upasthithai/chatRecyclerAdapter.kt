package com.example.upasthithai

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView

class chatRecyclerAdapter (private val chatList : List<recyclerdataclass>) :
    RecyclerView.Adapter<chatRecyclerAdapter.eventViewholder>() {
    class eventViewholder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val user = itemView.findViewById<TextView>(R.id.mysidechat)
        val bot = itemView.findViewById<TextView>(R.id.botsidechat)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): eventViewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.botchatbubble,parent,false)
        return eventViewholder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: eventViewholder, position: Int) {
    holder.bot.text = chatList[position].message
    holder.user.text = chatList[position].sender
    }
}