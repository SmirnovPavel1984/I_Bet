package com.smirnovpavel.ibet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smirnovpavel.ibet.data.Message
import com.smirnovpavel.ibet.databinding.MessageItemBinding

class MessageAdapter : ListAdapter<Message, MessageAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder (private val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind (message: Message) = with (binding) {
            tvMessage.text = message.messageText
            if (message.messageAuthor == Firebase.auth.currentUser?.uid) {
                cardView.setCardBackgroundColor (0xFFEBF4FF.toInt())
            } else {
                cardView.setCardBackgroundColor(0xFFFFEBEB.toInt())

            }
        }

        companion object {
            fun create (parent: ViewGroup): ItemHolder {
                return ItemHolder(MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<Message>(){
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

}