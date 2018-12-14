package biko.pougala.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class Scoreboard: Fragment() {

    private var firestoreFriends: FirebaseFirestore? = null
    private var firestoreInfo: FirebaseFirestore? = null
    private val tag_ = "Scoreboard"
    private var scores = HashMap<Int, String>()
    private var times = HashMap<String, Int>()
    private var tableFriends: TableLayout? = null
    private var firestore : FirebaseFirestore? = null
    private var mapFriends: MutableMap<Int, Int> = mutableMapOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.scoreboard, container, false)

        firestoreFriends = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        firestoreFriends?.firestoreSettings = settings
        tableFriends = view.findViewById(R.id.friends_table)
        scores = hashMapOf()
        times = hashMapOf()

        val username = coins.username
        val friendsRef =
            firestoreFriends?.collection("users-bank")?.document(username)?.collection("friends")?.document("friends")
        friendsRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                val friends = document.data?.toMap()
                var numberFriends = 0 // we'll use this value to programmatically create rows for the table

                @Suppress("UNCHECKED_CAST") // we're confident this is indeed a List
                val friendsList: List<String> = friends?.get("username") as List<String>
                if (friendsList != null) {
                    for (friend in friendsList) {

                        getCoins(friendsList, object: myCallBack {
                            override fun onCallback(value: Map<Int, Int>) {
                                Log.d(tag_, "the map is ${value.toString()}")
                            }
                        })

/*
                        val totalCoins = getCoins(friend)
                        val coinCounter = totalCoins.keys
                        val time = totalCoins.values

                        Log.d(tag_, "The coins that are returned are {$totalCoins.toString()}")
                        scores.put(coinCounter.first(), friend)
                        times.put(friend, time.first())*/
                    }

                    designTable(mapFriends)

                    Log.d(tag_, "Here is the final map: ${mapFriends.toString()}")

                    //val sortedScores = scores.toSortedMap()

                  /*  for ((score, name) in sortedScores) {
                        //TODO: add TableRow
                        val friendsRow = inflater.inflate(R.layout.friends_table, container, false)
                        val friendName: TextView = friendsRow.findViewById(R.id.username_friend)
                        val friendCoins: TextView = friendsRow.findViewById(R.id.coinCounterFriend)
                        val friendTime: TextView = friendsRow.findViewById(R.id.averageTime)

                        val time = times.get(name)
                        friendName.text = name
                        friendTime.text = time.toString()
                        friendCoins.text = score.toString()

                        tableFriends?.addView(friendsRow)

                    }*/
                }
            }


        }

        return view
    }



    private fun getCoins(usernames: List<String>, myCallBack: myCallBack) {


        val today = LocalDate.now().toString()



        // We'll get the start of the current week
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startOfWeek = cal.timeInMillis
        val startAsDate = cal.time.toString()

        firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        firestore?.firestoreSettings = settings

        for (username in usernames) {

            val coinsRef = firestore?.collection("users-bank")?.document(username)?.collection("coins")


            var totalCoins = 0
            var duration = 0


            coinsRef?.get()?.addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val documents = task.result?.documents!!
                    for (document in documents) {
                        val id = document.id
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                        val date = formatter.parse(id)
                        val timeStampDay = date.time
                        if (timeStampDay >= startOfWeek) { // if the date is withing a 7-day time frame

                            totalCoins += document.get("totalCoins").toString().toInt()
                            duration += document.get("duration").toString().toInt()


                        }
                    }
                    mapFriends.put(totalCoins, duration)

                } else {

                }


            }
        }
        myCallBack.onCallback(mapFriends)


    }

    private fun designTable(map: MutableMap<Int, Int>) {

        Log.d(tag, "the map is ${map.toString()}")


    }



    companion object {
        fun newInstance(): Scoreboard = Scoreboard()
    }
}

