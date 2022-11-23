package com.example.hanstargram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hanstargram.R
import com.example.hanstargram.databinding.FragmentDetailBinding
import com.example.hanstargram.navigation.model.AlarmDTO
import com.example.hanstargram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var user: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        user = FirebaseAuth.getInstance().currentUser

        binding.detailviewfragmentRecyclerview.adapter = DetailViewRecyclerViewAdapter()
        binding.detailviewfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {

            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val viewHolder = (holder as CustomViewHolder).itemView

            // Profile Image 가져오기
            firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                ?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val url = task.result["image"]
                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(viewHolder.findViewById(R.id.detailviewitem_profile_image))

                    }
                }

            //UserFragment로 이동
            viewHolder.findViewById<ImageView>(R.id.detailviewitem_profile_image)
                .setOnClickListener {

                    val fragment = UserFragment()
                    val bundle = Bundle()

                    bundle.putString("destinationUid", contentDTOs[position].uid)
                    bundle.putString("userId", contentDTOs[position].userId)

                    fragment.arguments = bundle
                    activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment)
                        .commit()
                }

            // 유저 아이디
            viewHolder.findViewById<TextView>(R.id.detailviewitem_profile_textview).text =
                contentDTOs[position].userId

            // 가운데 이미지
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .into(viewHolder.findViewById(R.id.detailviewitem_imageview_content))

            // 설명 텍스트
            viewHolder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text =
                contentDTOs[position].explain
            // 좋아요 이벤트
            viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview)
                .setOnClickListener { favoriteEvent(position) }

            //좋아요 버튼 설정
            if (contentDTOs[position].favorites.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)) {

                viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview)
                    .setImageResource(R.drawable.ic_favorite)

            } else {

                viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview)
                    .setImageResource(R.drawable.ic_favorite_border)
            }
            //좋아요 카운터 설정
            viewHolder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text =
                "좋아요 " + contentDTOs[position].favoriteCount + "개"

            viewHolder.findViewById<ImageView>(R.id.detailviewitem_comment_imageview)
                .setOnClickListener { v ->
                    var intent = Intent(v.context, CommentActivity::class.java)
                    intent.putExtra("contentUid", contentUidList[position])
                    startActivity(intent)
                }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        //좋아요 이벤트 기능
        private fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                // 좋아요 버튼 눌려있을 때
                if (contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO.favoriteCount = contentDTO.favoriteCount!! - 1
                    contentDTO.favorites.remove(uid)

                    // 좋아요 버튼 안눌려있을 때
                } else {
                    contentDTO.favoriteCount = contentDTO.favoriteCount!! + 1
                    contentDTO.favorites[uid] = true
//                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid: String) {
            val alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = user?.email
            alarmDTO.uid = user?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}