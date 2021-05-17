package com.nac.whatappkotlin.message

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.nac.whatappkotlin.R
import com.nac.whatappkotlin.model.ChatMessage
import com.nac.whatappkotlin.model.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogAct : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageAct.USER_KEY)
        if (toUser != null) {
            supportActionBar?.title = toUser?.uid
        }

//        setupDummyData()
        listenForMessage()

        btn_send.setOnClickListener {
            Log.d(TAG, "Attempt to send message...")
            performSendMessage()
        }
    }

    private fun listenForMessage() {

        val ref = FirebaseDatabase.getInstance().getReference("messages")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser= LatesMessageAct.currentUser?:return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }


    private fun performSendMessage() {
        //how to we actually send a message to firebase
        val text = editText_chat_log.text.toString()
        val refer = FirebaseDatabase.getInstance().getReference("/messages").push()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageAct.USER_KEY)
        val toId = user?.uid

        if (fromId == null) return

        val chatMessage = ChatMessage(
            refer.key!!, text, fromId, toId,
            System.currentTimeMillis() / 1000
        )
        refer.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat message: ${refer.key}")
        }
    }

    class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView_from_row.text = text

            //Load our user image into the star
            val uri = user.profileImageUrl
            val targetImageview = viewHolder.itemView.image_chat_from_row
            Picasso.get().load(uri).into(targetImageview)

        }

        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }
    }

    class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView_to_row.text = text

            //Load our user image into the star
            val uri = user.profileImageUrl
            val targetImageview = viewHolder.itemView.image_chat_to_row
            Picasso.get().load(uri).into(targetImageview)

        }

        override fun getLayout(): Int {
            return R.layout.chat_to_row
        }
    }
}


