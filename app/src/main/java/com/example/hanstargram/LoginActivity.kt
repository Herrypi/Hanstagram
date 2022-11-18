package com.example.hanstargram

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
//import com.example.filwallet.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        signupButton.setOnClickListener {
            signinAndSignup()
        }
        googleSignupButton.setOnClickListener {
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
    }
    //구글 로그인 부분 -> 버튼 클릭시 데이터가 옮겨오는 부분 오류 이유 ..?
    //----------------------------------------------------------------------------------------------------
    fun googleLogin() {
        var signInIntent: Intent = googleSignInClient!!.signInIntent
        startForResult.launch(signInIntent)
//
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent: Intent = result.data!!
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try{
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(ContentValues.TAG, " " + account.id)
                    firebaseAuthWithGoogle(account)

                }catch (e: ApiException){
                    Log.w(ContentValues.TAG, "", e)
                }
            }
        }
    //----------------------------------------------------------------------------------------------------
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task->
                if(task.isSuccessful) {
                    moveMainPage(task.result?.user)
                }
                else{
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(emailEditText.text.toString(), passwdEditText.text.toString())
            ?.addOnCompleteListener {
                    task->
                if(task.isSuccessful) {
                    //아이디가 생성되었을때 필요한 코드 입력
                    moveMainPage(task.result?.user)
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
                    moveMainPage(task.result?.user)
                }
                else{
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
    fun moveMainPage(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }
}