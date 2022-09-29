package uz.bdm.kotlinmessenger.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.lates_message_item_layout.view.*
import uz.bdm.kotlinmessenger.R

class LatesMesagesRow(val chatMessage: ChatMessageModel) : Item<GroupieViewHolder>() {
    var chartPArtnerUser:UserModel?=null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.text_latest_mesage.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chartPArtnerUser = snapshot.getValue(UserModel::class.java)
                viewHolder.itemView.username_lates_message.text = chartPArtnerUser?.username
                Picasso.get().load(chartPArtnerUser?.profileImageUrl)
                    .into(viewHolder.itemView.image_lates_message)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.lates_message_item_layout
    }
}
