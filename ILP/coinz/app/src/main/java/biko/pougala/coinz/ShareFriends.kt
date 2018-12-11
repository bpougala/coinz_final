package biko.pougala.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.TableLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.jetbrains.anko.find

class ShareFriends: Fragment() {

    private var firestore: FirebaseFirestore? = null
    private var firestoreFriends: CollectionReference? = null
    private var headline: TextView? = null
    private lateinit var friends: MutableList<String> // this list will hold the username of every friend in the user's database
    private var add_friend_message: TextView? = null
    private var add_friend_button: Button? = null
    private var friendsTable: TableLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.share_friends, container, false)
        headline = view.findViewById(R.id.headline)
        add_friend_button = view.findViewById(R.id.add_friend_button)
        add_friend_message = view.findViewById(R.id.add_friend_message)
        friendsTable = view.findViewById(R.id.friends_table)

        // First we'll check in the database if the user has added friends to the game. Therefore the button and message
        // need to be invisible when the app is first loaded
        add_friend_button?.visibility = View.GONE
        add_friend_message?.visibility = View.GONE

        val coinCounter = coins.coinCounter
        headline?.text = getString(R.string.send_to_friends, coinCounter)

        // check the database to get the list of game friends
        firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()

        firestore?.firestoreSettings = settings

        val friendsRef = firestore?.collection("users-bank")?.document()
        return view

    }

    companion object {
        fun newInstance(): ShareFriends = ShareFriends()
    }
}