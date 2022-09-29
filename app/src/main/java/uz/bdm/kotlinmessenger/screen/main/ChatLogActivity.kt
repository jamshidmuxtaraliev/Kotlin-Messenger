package uz.bdm.kotlinmessenger.screen.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.message_from_row_layout.view.*
import kotlinx.android.synthetic.main.message_too_row_layout.view.*
import uz.bdm.kotlinmessenger.R
import uz.bdm.kotlinmessenger.model.ChatMessageModel
import uz.bdm.kotlinmessenger.model.UserModel
import uz.bdm.kotlinmessenger.utils.Constants

class ChatLogActivity : AppCompatActivity() {
    var toUser: UserModel? = null
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<UserModel>(Constants.EXTRA_DATA)
        userNamePartner.text = toUser?.username

        imgBack.setOnClickListener {
            val intent = Intent(this, LatesMessagesActivity::class.java)
            startActivity(intent)
            finish()
        }

        listenForMessage()

        imageView.setOnClickListener {
            if (new_message.text.toString() == ""){
                return@setOnClickListener
            }else{
                performSendMessage()
            }
        }

        recyclerMessages.adapter = adapter
    }

    fun performSendMessage() {
        var text = new_message.text.toString()
        var fromId = FirebaseAuth.getInstance().uid
        var toId = toUser?.uid ?: ""
        if (fromId == null) return

        var ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        var toRef =
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessages =
            ChatMessageModel(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        ref.setValue(chatMessages).addOnSuccessListener {
            Log.d("MES", "${ref.key}")
            new_message.text.clear()
            recyclerMessages.scrollToPosition(adapter.itemCount - 1)
        }
        toRef.setValue(chatMessages)

        //last messages
        val refFromLastMessage =
            FirebaseDatabase.getInstance().getReference("/last-messages/$fromId/$toId")
        refFromLastMessage.setValue(chatMessages)

        val refToLastMessage =
            FirebaseDatabase.getInstance().getReference("/last-messages/$toId/$fromId")
        refToLastMessage.setValue(chatMessages)
    }

    fun listenForMessage() {
        var fromId = FirebaseAuth.getInstance().uid
        var toId = toUser?.uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessageModel::class.java)
                if (chatMessage != null) {
                    Log.d("CHAT", chatMessage.toString())
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatesMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToRow(chatMessage.text, toUser!!))
                        recyclerMessages.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

class ChatFromItem(val text: String, val user: UserModel) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.new_message_from_row.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.from_profile_image)
    }

    override fun getLayout(): Int {
        return R.layout.message_from_row_layout
    }
}

class ChatToRow(val text: String, val user: UserModel) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.new_message_too_row.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.too_profile_image)
    }

    override fun getLayout(): Int {
        return R.layout.message_too_row_layout
    }

}