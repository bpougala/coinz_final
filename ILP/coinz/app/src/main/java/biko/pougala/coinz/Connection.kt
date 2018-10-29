package biko.pougala.coinz

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import biko.pougala.coinz.R.id.email
import biko.pougala.coinz.R.id.password
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

const val EXTRA_MESSAGE = "biko.pougala.coinz.MESSAGE"
class Connection : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var emailBar: EditText = findViewById((R.id.email))
    private var passwordBar: EditText = findViewById(R.id.password)
    private var signInButton: Button = findViewById(R.id.email_sign_in_button)

    private var email: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        signInButton.setOnClickListener {
            email = emailBar.text.toString()
            password = passwordBar.text.toString()
            authenticate(email, password)
        }
    }




    override fun onStart() {
        super.onStart()
        if (mAuth != null) {
            val currentUser: FirebaseUser? = mAuth!!.currentUser

        } else {
            mAuth = FirebaseAuth.getInstance()
            val currentUser: FirebaseUser? = mAuth!!.currentUser

        }

    }

    fun authenticate(email: String?, password: String?) {

        // if the user doesn't enter any input, display an alert message
        if (email == null || password == null) {
            val builder = AlertDialog.Builder(this@Connection)
            builder.setTitle("No email/password entered")
            builder.setMessage("Please make sure you properly entered an email and a password before trying again.")
            updateUI(null)

        } else {
            mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // sign in success, update UI with user info
                        updateUI(mAuth?.currentUser)
                    } else {
                        // sign in failed, display a message to the user
                        val builder = AlertDialog.Builder(this@Connection)
                        builder.setTitle("Wrong credentials")
                        builder.setMessage("Your email address or password is incorrect. Please review them before trying again.")
                    }
                }
        }
    }


    fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val userName: String? = user.displayName
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(biko.pougala.coinz.EXTRA_MESSAGE, user)
             }
            startActivity(intent)

        }

    }


}