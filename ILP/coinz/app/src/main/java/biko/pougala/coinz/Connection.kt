package biko.pougala.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

const val EXTRA_MESSAGE = "biko.pougala.coinz.MESSAGE"


class Connection : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private var email: String? = null
    private var password: String? = null
    private var username: String? = null
    private var emailBarSignIn: EditText? = null
    private var passwordBarSignIn: EditText? = null
    private var signInButton: Button? = null
    private var emailBarRegister: EditText? = null
    private var passwordBarRegister: EditText? = null
    private var usernameBar: EditText? = null
    private var registerButton: Button? = null


    private var firestore: FirebaseFirestore? = null
    private var firestoreChat: DocumentReference? = null

    companion object {
        private const val TAG = "Connection"
        private const val COLLETION_KEY = "coinz"
        private const val DOCUMENT_KEY = "bank"
        private const val GOLD_FIELD = 0.0
        private const val USERNAME_FIELD = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailBarSignIn = findViewById((R.id.email))
        passwordBarSignIn = findViewById(R.id.password)
        signInButton = findViewById(R.id.email_sign_in_button)

        emailBarRegister = findViewById(R.id.email2)
        passwordBarRegister = findViewById(R.id.password2)
        registerButton = findViewById(R.id.register)

        mAuth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()

        signInButton?.setOnClickListener {
            email = emailBarSignIn?.text.toString()
            password = passwordBarSignIn?.text.toString()
            authenticate(email, password)
        }

        registerButton?.setOnClickListener {
            email = emailBarRegister?.text.toString()
            password = passwordBarRegister?.text.toString()
            username = usernameBar?.text.toString()
            createUser(email, password, username)
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

    fun createUser(email: String?, password: String?, username: String?) {
        if (email == null || password == null || username == null) {
            val builder = AlertDialog.Builder(this@Connection)
            builder.setTitle("No email/password entered")
            builder.setMessage("Please make sure you properly entered an email, a password and a username before trying again.")
            updateUI(null)
        } else {
            mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        updateUI(mAuth?.currentUser)
                        //TODO: add the username to the database

                        var db = HashMap<String, Any>()
                        db.put("gold", 0.0) // by default, all new users have 0 GOLD in the bank
                        db.put("username", username)

                    } else {
                        // sign in failed, display a message to the user
                        val builder = AlertDialog.Builder(this@Connection)
                        builder.setTitle("Wrong credentials")
                        builder.setMessage("Your email address or password is incorrect. Please review them before trying again.")
                    }
                }
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