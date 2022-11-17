package com.example.hanstargram

import androidx.appcompat.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.example.filwallet.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        signupButton.setOnClickListener {
            signinAndSignup()
        }
    }
    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(emailEditText.text.toString(), passwdEditText.text.toString())
            ?.addOnCompleteListener {
                    task->
                if(task.isSuccessful) {
                    //아이디가 생성되었을때 필요한 코드 입력
                    moveMainPage(task.result.user)
                }else if(task.exception?.message.isNullOrEmpty()) { //로그인 에러났을때 메세지 출력
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }else {
                    signinEmail()
                }
            }
    }
    fun signinEmail() {
        auth?.signInWithEmailAndPassword(emailEditText.text.toString(), passwdEditText.text.toString())
            ?.addOnCompleteListener{
                task->
                if(task.isSuccessful) {
                    moveMainPage(task.result.user)
                }
                else{
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
    fun moveMainPage(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}