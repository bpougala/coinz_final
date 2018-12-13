package biko.pougala.coinz

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
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
    private var tag_ = "ShareFriends"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val username = coins.username
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        headline = view.findViewById(R.id.total_weekly)
        val spareCounter = coins.spareChange.size

        add_friend_message = view.findViewById(R.id.add_friend_message)
        add_friend_button = view.findViewById(R.id.add_friend_button)
        friendsTable = view.findViewById(R.id.friends_table)
        headline?.text = getString(R.string.send_coins_message, spareCounter)

        //   headline?.text = getString(R.string.sendFriends, coinCounter)
       // add_friend_button = view.findViewById(R.id.add_friend_button)
       // add_friend_message = view.findViewById(R.id.add_friend_message)
      //  friendsTable = view.findViewById(R.id.friends_table)
        // prepare the view for the AlertDialog coming on line 77 to confirm the emission of coins
        val factory = LayoutInflater.from(activity)
        val alertView = factory.inflate(R.layout.send_coin_friend, null)


        // First we'll check in the database if the user has added friends to the game. Therefore the button and message
        // need to be invisible when the app is first loaded
        add_friend_button?.visibility = View.GONE
        add_friend_message?.visibility = View.GONE



        // check the database to get the list of game friends
        firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()

        firestore?.firestoreSettings = settings

        val friendsRef = firestore?.collection("users-bank")?.document(username)?.collection("friends")?.document("friends")
        friendsRef?.get()?.addOnSuccessListener{ document ->
            if (document != null) {
                //TODO: populate the TableView with the list of friends
                friendsTable?.visibility = View.VISIBLE

                @Suppress("UNCHECKED_CAST")
                val friends: List<String> = document.get("username") as List<String>


                if(friends != null) {
                    for (name in friends) {
                        val friendsRow = View.inflate(activity, R.layout.friends_table, null)


                        val friendName: TextView = friendsRow.findViewById(R.id.username_friend)
                        friendName.text = name.toString()

                        friendsTable?.addView(friendsRow)

                        val friendButton: Button = friendsRow.findViewById(R.id.send_button_friend)


                        friendButton.setOnClickListener {
                            Log.d(tag_, "The button has been clicked")
                            val builder = AlertDialog.Builder(activity)
                            builder.setView(alertView)
                            builder.setPositiveButton("Confirm") { dialog, which ->
                                //TODO send coins to the user selected on Firestore

                            }

                        }
                    }
                }


            } else {
                add_friend_button?.visibility = View.VISIBLE
                add_friend_message?.visibility = View.VISIBLE
            }
        }

        add_friend_button?.setOnClickListener {
            val intent = Intent(activity, AddFriends::class.java)
            startActivity(intent)
        }


        return view

    }



    companion object {
        fun newInstance(): ShareFriends {
            val fragment = ShareFriends()
            return fragment
        }
    }
}