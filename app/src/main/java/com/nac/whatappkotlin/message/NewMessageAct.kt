package com.nac.whatappkotlin.message

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nac.whatappkotlin.R
import com.nac.whatappkotlin.model.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*


class NewMessageAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"
        fetchUsers()
    }
   companion object{
       val USER_KEY= "USER_KEY"
   }
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/user")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                    adapter.setOnItemClickListener { item, view ->

                        val userItem=item as UserItem
                        val intent = Intent(view.context, ChatLogAct::class.java)
                        intent.putExtra(USER_KEY,userItem.users)
                        startActivity(intent)
                        finish()
                    }
                }
                recyclerview_message.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    class UserItem(val users: User) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_textview_messgae.text = users.username
            Picasso.get().load(users.profileImageUrl).into(viewHolder.itemView.image_view_message)
        }

        override fun getLayout(): Int {
            return R.layout.user_row_new_message
        }
    }
}

