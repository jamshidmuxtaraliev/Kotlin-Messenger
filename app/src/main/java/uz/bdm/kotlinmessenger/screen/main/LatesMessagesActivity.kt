package uz.bdm.kotlinmessenger.screen.main

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_lates_messages.*
import kotlinx.android.synthetic.main.lates_message_item_layout.view.*
import kotlinx.android.synthetic.main.navigation_view.view.*
import uz.bdm.kotlinmessenger.R
import uz.bdm.kotlinmessenger.model.ChatMessageModel
import uz.bdm.kotlinmessenger.model.LatesMesagesRow
import uz.bdm.kotlinmessenger.model.UserModel
import uz.bdm.kotlinmessenger.screen.autentification.RegisterActivity
import uz.bdm.kotlinmessenger.utils.Constants

class LatesMessagesActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()
    val lastMessageHashMap = HashMap<String, ChatMessageModel>()

    companion object {
        var currentUser: UserModel? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lates_messages)

        verifyUserIsLogged()
        fetchCurrentUser()
        listenLatesMessages()

        imgMenu.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        lyNull.setOnClickListener{
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
        }

        navigation.lyContacts.setOnClickListener {
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
        }

        navigation.lyLog_out.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }

        recycler_lates_messages.adapter = adapter
        adapter.setOnItemClickListener { item, view ->
            val row = item as LatesMesagesRow
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(Constants.EXTRA_DATA, row.chartPArtnerUser)
            startActivity(intent)
        }
        recycler_lates_messages.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    fun verifyUserIsLogged() {
        var uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(UserModel::class.java)
                navigation.my_userName.text = currentUser?.username
                Picasso.get().load(currentUser?.profileImageUrl).into(navigation.my_profile_image)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun listenLatesMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/last-messages/$fromId")
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessages = snapshot.getValue(ChatMessageModel::class.java) ?: return
                lastMessageHashMap[snapshot.key!!] = chatMessages
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessageModel::class.java) ?: return
                lastMessageHashMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun refreshRecyclerView() {
        adapter.clear()
        lastMessageHashMap.values.forEach {
            adapter.add(LatesMesagesRow(it))
        }

        lyNull.visibility =if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        recycler_lates_messages.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
    }
}