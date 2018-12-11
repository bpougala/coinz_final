package biko.pougala.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button 
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class ShareFriends: Fragment() {

    private var firestore: FirebaseFirestore? = null
    private var firestoreFriends: CollectionReference? = null
    private var headline: TextView? = null
    private lateinit var friends: MutableList<String> // this list will hold the username of every friend in the user's database
    private val add_friend_message: TextView? = null
    private val add_friend_button: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.share_friends, container, false)
        headline = view.findViewById(R.id.headline)
        val coinCounter = coins.coinCounter
        headline?.text = getString(R.string.send_to_friends, coinCounter)
        return view

    }

    companion object {
        fun newInstance(): ShareFriends = ShareFriends()
    }
}