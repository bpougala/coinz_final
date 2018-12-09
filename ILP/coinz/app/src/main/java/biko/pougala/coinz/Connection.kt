package biko.pougala.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.jetbrains.anko.toast
import timber.log.Timber

const val EXTRA_MESSAGE = "biko.pougala.coinz.MESSAGE"


class Connection : AppCompatActivity() {


    private val tag = "Connection"
    private var mAuth: FirebaseAuth? = null

    private var email: String? = null
    private var password: String? = null
    private var username: String? = null
    private var usernameBarSignIn: EditText? = null
    private var passwordBarSignIn: EditText? = null
    private var signInButton: Button? = null
    private var emailBarRegister: EditText? = null
    private var passwordBarRegister: EditText? = null
    private var usernameBar: EditText? = null
    private var registerButton: Button? = null


    private var firestore: FirebaseFirestore? = null
    private var firestoreCoinz: DocumentReference? = null

    companion object {
        private const val TAG = "users-bank"
        private const val COLLECTION_KEY = "users-bank"
        private const val DOCUMENT_KEY = "bank"
        private const val GOLD_FIELD = 0.0
        private const val USERNAME_FIELD = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usernameBarSignIn = findViewById((R.id.username_2))
        passwordBarSignIn = findViewById(R.id.password)
        signInButton = findViewById(R.id.email_sign_in_button)

        emailBarRegister = findViewById(R.id.email2)
        passwordBarRegister = findViewById(R.id.password2)
        usernameBar = findViewById(R.id.username)
        registerButton = findViewById(R.id.register)

        mAuth = FirebaseAuth.getInstance()

        // set up Firestore to access our database to either get or create a username
        firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        firestore?.firestoreSettings = settings

        firestoreCoinz = firestore?.collection(COLLECTION_KEY)?.document(DOCUMENT_KEY)

        signInButton?.setOnClickListener {
            username = usernameBarSignIn?.text.toString()
            password = passwordBarSignIn?.text.toString()
            authenticate(username, password)
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
            updateUI(null)
        } else {
            mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        firestoreCoinz = firestore?.collection("users-bank")
                            ?.document(username)

                        val db = mapOf(
                            "gold" to 0.0,
                            "email" to email
                        )

                        firestoreCoinz?.set(db)
                            ?.addOnSuccessListener {
                                Log.d(tag, "Username successfully added") }
                            ?.addOnFailureListener { e -> Log.e(tag, e.message)}
                        updateUI(mAuth?.currentUser)

                    } else {
                        // sign in failed, display a message to the user
                        val builder = AlertDialog.Builder(this@Connection)
                        builder.setTitle("An account already exists")
                        builder.setMessage("Please either change your username or sign in with the current email address.")
                        builder.show()
                    }
                }
        }
    }

    fun authenticate(username: String?, password: String?) {

        // if the user doesn't enter any input, display an alert message
        if (username == null || password == null) {
            val builder = AlertDialog.Builder(this@Connection)
            builder.setTitle("No email/password entered")
            builder.setMessage("Please make sure you properly entered an email and a password before trying again.")
            builder.show()
            updateUI(null)

        } else {
            firestoreCoinz = firestore?.collection("users-bank")?.document(username)

            firestoreCoinz?.get()?.addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    val document = task.result
                    Log.d(tag, "username from result = $username")
                    if(document != null) {
                        val email = document.get("email").toString()
                        Log.d(tag,"email= $email")
                        Timber.d("password= $email")
                        mAuth?.signInWithEmailAndPassword(email, password)
                            ?.addOnCompleteListener(this) { task_ ->
                                if(task_.isSuccessful) {
                                    Log.d(tag, "Sign in successful")
                                    updateUI(mAuth?.currentUser)

                                } else {
                                    Log.d(tag, "Sign in unsucessful")
                                    val builder = AlertDialog.Builder(this@Connection)
                                    builder.setTitle("Wrong credentials")
                                    builder.setMessage("An error occured while logging you in. Please review your credentials and try again.")
                                    builder.show()
                                }
                            }
                    }
                }

            }

        }
    }


    fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val newEmail: String? = user.email
            Log.d(tag, "newEmail = $newEmail")
            firestoreCoinz = firestore?.collection(COLLECTION_KEY)
                ?.document(newEmail!!)

            firestoreCoinz?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val details = task.result

                }
            }
            val intent = Intent(this@Connection, MainActivity::class.java)
            intent.putExtra("username", username)
            Timber.d("username = $username")

            startActivity(intent)

        } else {
            // this case is triggered when an error occured but is unknown
            val builder = AlertDialog.Builder(this@Connection)
            builder.setTitle("An error occured")
            builder.setMessage("Please try again later.")
        }

    }

}