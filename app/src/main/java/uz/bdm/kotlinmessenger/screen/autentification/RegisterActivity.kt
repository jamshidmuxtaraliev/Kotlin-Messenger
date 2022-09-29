package uz.bdm.kotlinmessenger.screen.autentification

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import uz.bdm.kotlinmessenger.databinding.ActivityRegisterBinding
import uz.bdm.kotlinmessenger.model.UserModel
import uz.bdm.kotlinmessenger.screen.main.LatesMessagesActivity
import java.util.*

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    var selectedPhotoUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        binding.btnSign.setOnClickListener {
            var email = binding.email.text.toString()
            var password = binding.password.text.toString()

            if (email == "" || password == ""){
                Toast.makeText(this, "Cannot null Login or Password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (selectedPhotoUri == null) {
                Toast.makeText(this, "No selected profile image.", Toast.LENGTH_SHORT).show()
            }
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        uploadImageToFirebaseStorage()
                    }else{
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(baseContext, "Failed caused by: \n ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    fun uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null) return
         val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { task->
                ref.downloadUrl.addOnSuccessListener {
                    saveUserTOFirebase(it.toString())
                }
            }
    }

    fun saveUserTOFirebase(imgUrl:String){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
        val user = UserModel(uid, binding.username.text.toString(), imgUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, LatesMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            binding.imageUser.setImageDrawable(bitmapDrawable)

            binding.imageUser.visibility = View.VISIBLE
            binding.textSelectFoto.visibility = View.GONE
        }
    }
}