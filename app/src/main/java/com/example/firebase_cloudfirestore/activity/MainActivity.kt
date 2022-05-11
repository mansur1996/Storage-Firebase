package com.example.firebase_cloudfirestore.activity

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.firebase_cloudfirestore.adapter.UserAdapter
import com.example.firebase_cloudfirestore.databinding.ActivityMainBinding
import com.example.firebase_cloudfirestore.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequest


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var reference: StorageReference
    private lateinit var firebaseFirestore : FirebaseFirestore
    private val TAG = "MainActivity"
    var profileUrl : String? = null
    private lateinit var userAdapter: UserAdapter
    private lateinit var list : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askPermission()
        initViews()
    }

    private fun askPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {


                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }
            }).check()
    }

    private fun initViews() {
        // to save image
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("images")

        //to save User object
        firebaseFirestore = FirebaseFirestore.getInstance()


        list = ArrayList()

        showProgressBar()
        readUsersList()

        binding.bntSave.setOnClickListener {
            saveUser()
        }

        binding.ivProfile.setOnClickListener {
            openFileChooser()
        }

    }

    private fun openFileChooser() {
        getImageContent.launch("image/*")
    }

    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
        binding.ivProfile.setImageURI(uri)
        val id = System.currentTimeMillis()
        val uploadTask = reference.child(id.toString()).putFile(uri)

        uploadTask.addOnSuccessListener {
            if(it.task.isSuccessful){
                val downloadUrl = it.metadata?.reference?.downloadUrl
                downloadUrl?.addOnSuccessListener { imgUri->
                    profileUrl = imgUri.toString()
                }
            }

        }.addOnFailureListener{
            Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUser() {
        showProgressBar()

        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().trim().toInt()

        val user = User(name, age, profileUrl)

        firebaseFirestore.collection("usersS")
            .add(user)
            .addOnSuccessListener { documentReference->
                Toast.makeText(this, "Successfully Added", Toast.LENGTH_SHORT).show()
                clearEditTexts()
                list.add(user)
                hideProgressBar()
                userAdapter.notifyItemInserted(list.size)
            }
            .addOnFailureListener { exception->
                Toast.makeText(this, "Error ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun readUsersList() {
        firebaseFirestore.collection("usersS")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result

                    result.forEach { queryDocumentSnapshot ->
                        val user = queryDocumentSnapshot.toObject(User::class.java)
                        list.add(user)
                    }

                    refreshAdapter(list)

                }else{
                    Log.w(TAG, "Error getting documents.", it.exception)
                }
            }
    }

    private fun refreshAdapter(list : ArrayList<User>) {
        userAdapter = UserAdapter(list)
        hideProgressBar()
        binding.rvUsers.adapter = userAdapter

    }

    private fun clearEditTexts() {
        binding.etName.setText("")
        binding.etAge.setText("")
    }

    private fun showProgressBar(){
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        binding.progressBar.visibility = View.GONE
    }
}