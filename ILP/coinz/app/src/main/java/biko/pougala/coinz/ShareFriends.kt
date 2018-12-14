package biko.pougala.coinz

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import org.jetbrains.anko.find
import kotlinx.android.synthetic.main.friends_table.*
import java.time.LocalDate

class ShareFriends: Fragment() {

    private var firestore: FirebaseFirestore? = null
    private var firestoreFriends: CollectionReference? = null
    private var headline: TextView? = null
    private lateinit var friends: MutableList<String> // this list will hold the username of every friend in the user's database
    private var sendCoinButton: Button? = null
    private var friendNameField: TextView? = null
    private var tag_ = "ShareFriends"
    private var numberPicker: NumberPicker? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val username = coins.username
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        headline = view.findViewById(R.id.total_weekly)
        val spareCounter = coins.spareChange.size

       // add_friend_message = view.findViewById(R.id.add_friend_message)
       // add_friend_button = view.findViewById(R.id.add_friend_button)
       // friendsTable = view.findViewById(R.id.friends_table)
        headline?.text = getString(R.string.send_coins_message, spareCounter)

        //   headline?.text = getString(R.string.sendFriends, coinCounter)
       // add_friend_button = view.findViewById(R.id.add_friend_button)
       // add_friend_message = view.findViewById(R.id.add_friend_message)
      //  friendsTable = view.findViewById(R.id.friends_table)


        // First we'll check in the database if the user has added friends to the game. Therefore the button and message
        // need to be invisible when the app is first loaded
        sendCoinButton = view.findViewById(R.id.button5)
        friendNameField = view.findViewById(R.id.editText)
        numberPicker = view.findViewById(R.id.numberPickerNew)
        numberPicker?.maxValue = spareCounter


        sendCoinButton?.setOnClickListener {

            sendCoinToFriend()


                }




        return view

    }

    fun sendCoinToFriend() {
        val name = friendNameField?.text.toString()
        val number = numberPicker?.value

        if(name != "" && number != 0) {
            firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
            firestore?.firestoreSettings = settings

            val today = LocalDate.now().toString()
            val coinsRef = firestore?.collection("users-bank")?.document(name)?.collection("coins")
                ?.document(today)

            var friendTotalCoin = 0 // we need to know how many coins the friend has today to increment it
            coinsRef?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val coinsCount = task.result?.data?.count()
                    if(coinsCount != null) {
                        friendTotalCoin = coinsCount.toInt() -1 // we can't directly use our field "totalCoins" created in
                        // MainActivity because we don't know if it exists so we'll count all elements minus potentially this field

                    }
                } else {

                }

            }

            val otherCoinsRef = firestore?.collection("users-bank")?.document(name)?.collection("coins")
                ?.document(today)
            val newCoin = mapOf(
                "gold_${friendTotalCoin}" to coins.spareChange.remove(),
                //    "username" to username
                "totalCoins" to friendTotalCoin+1
            )
            otherCoinsRef?.set(newCoin, SetOptions.merge())?.addOnCompleteListener {
                val toast = Toast.makeText(activity, "Coin sent!", Toast.LENGTH_LONG)
                toast.show()
            }?.addOnFailureListener {

                val toast = Toast.makeText(activity, "Failed: The user cannot receive a coin right now", Toast.LENGTH_LONG)
                toast.show()
            }


        }



    }









       companion object {
        fun newInstance(): ShareFriends {
            val fragment = ShareFriends()
            return fragment
        }
    }



}