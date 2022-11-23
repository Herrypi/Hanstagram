//package com.example.hanstargram.navigation
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.example.hanstargram.R
//import com.example.hanstargram.navigation.model.ContentDTO
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.firestore.FirebaseFirestore
//
//import kotlinx.android.synthetic.main.activity_comment.*
//import kotlinx.android.synthetic.main.item_comment.view.*
//
//
//class CommentActivity : AppCompatActivity() {
//
//    var contentUid: String? = null
//    var user: FirebaseUser? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_comment)
//
//        user = FirebaseAuth.getInstance().currentUser
//        contentUid = intent.getStringExtra("contentUid")
//
//        comment_btn_send.setOnClickListener {
//            val comment = ContentDTO.Comment()
//
//            comment.userId = FirebaseAuth.getInstance().currentUser!!.email
//            comment.comment = comment_edit_message.text.toString()
//            comment.uid = FirebaseAuth.getInstance().currentUser!!.uid
//            comment.timestamp = System.currentTimeMillis()
//
//            FirebaseFirestore.getInstance()
//                .collection("images")
//                .document(contentUid!!)
//                .collection("comments")
//                .document()
//                .set(comment)
//
//            comment_edit_message.setText("")
//
//        }
//
//        comment_recyclerview.adapter = CommentRecyclerViewAdapter()
//        comment_recyclerview.layoutManager = LinearLayoutManager(this)
//
//    }
//
//
//
//
//
//    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//        val comments: ArrayList<ContentDTO.Comment>
//
//        init {
//            comments = ArrayList()
//            FirebaseFirestore
//                .getInstance()
//                .collection("images")
//                .document(contentUid!!)
//                .collection("comments")
//                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                    comments.clear()
//                    if (querySnapshot == null) return@addSnapshotListener
//                    for (snapshot in querySnapshot?.documents!!) {
//                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
//                    }
//                    notifyDataSetChanged()
//
//                }
//
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.item_comment, parent, false)
//            return CustomViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//
//            var view = holder.itemView
//
//            // Profile Image
//            FirebaseFirestore.getInstance()
//                .collection("profileImages")
//                .document(comments[position].uid!!)
//                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
//                    if (documentSnapshot?.data != null) {
//
//                        val url = documentSnapshot?.data!!["image"]
//                        Glide.with(holder.itemView.context)
//                            .load(url)
//                            .apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
//                    }
//                }
//
//            view.commentviewitem_textview_profile.text = comments[position].userId
//            view.commentviewitem_textview_comment.text = comments[position].comment
//        }
//
//        override fun getItemCount(): Int {
//
//            return comments.size
//        }
//
//        private inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
//    }
//}

package com.example.hanstargram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hanstargram.R
import com.example.hanstargram.databinding.ActivityCommentBinding
import com.example.hanstargram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CommentActivity : AppCompatActivity() {
    private var binding: ActivityCommentBinding? = null
    var contentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        contentUid = intent.getStringExtra("contentUid")

        binding!!.commentRecyclerview.adapter = CommentRecyclerViewAdapter()
        binding!!.commentRecyclerview.layoutManager = LinearLayoutManager(this)

        binding!!.commentBtnSend.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding!!.commentEditMessage.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)

            binding!!.commentEditMessage.setText("")
        }

    }

    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).text =
                comments[position].comment
            view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text =
                comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(view.findViewById(R.id.commentviewitem_imageview_profile))
                    }
                }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
}