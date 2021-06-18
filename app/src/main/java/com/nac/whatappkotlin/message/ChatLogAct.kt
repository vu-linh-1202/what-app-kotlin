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
import com.nac.whatappkotlin.views.ChatFromItem
import com.nac.whatappkotlin.views.ChatToItem
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

        listenForMessage()

        btn_send.setOnClickListener {
            Log.d(TAG, "Attempt to send message...")
            performSendMessage()
        }
    }

    private fun listenForMessage() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser= LatesMessageAct.currentUser?:return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
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
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageAct.USER_KEY)
        val toId = user?.uid

        if (fromId == null) return
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val refer = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/${toId}")
        val chatMessage = ChatMessage(refer.key!!, text, fromId, toId, System.currentTimeMillis() / 1000
        )

        refer.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat message: ${refer.key}")
            editText_chat_log.text.clear()
            recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
        }
        toReference.setValue(chatMessage)

        val latesMessageRef= FirebaseDatabase.getInstance().getReference("lates-messsge/$fromId/$toId")
        latesMessageRef.setValue(chatMessage)
        val latesMessageToRef= FirebaseDatabase.getInstance().getReference("lates-message/$toId/$fromId")
        latesMessageToRef.setValue(chatMessage)
    }

}


