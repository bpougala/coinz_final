package biko.pougala.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class Scoreboard: Fragment() {

    private var firestoreFriends: FirebaseFirestore? = null
    private val tag_ = "Scoreboard"
    private var scores = HashMap<String, Int>()
    private var times = HashMap<String, Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.scoreboard, container, false)

        firestoreFriends = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        firestoreFriends?.firestoreSettings = settings

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
                        numberFriends++
                        val totalCoins = getCoins(friend)
                        scores.put(friend, totalCoins.get(0))
                        times.put(friend, totalCoins.get(1))
                        val tableView = inflater.inflate(R.layout.friends_table, container, false)
                    }

                    val sortedScores = scores.toList().sortedBy { (key, value) -> value }.toMap()

                    for (score in sortedScores) {
                        
                    }
                }
            }
        }

        return view
    }



    private fun getCoins(username: String): Array<Int> {
        val today = LocalDate.now().toString()
        val coinsRef = firestoreFriends?.collection("users-bank")?.document(username)?.collection("coins")

        // We'll get the start of the current week
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startOfWeek = cal.timeInMillis
        val startAsDate = cal.time.toString()

        var coinCounter = 0
        var totalTime = 0

        coinsRef?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                // In order to only get dates within a specific timeframe (starting with the current week-, we convert all dates to timestamps
                // and do simple numeric comparisons


                val id = document.id
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                val date = formatter.parse(id)
                val timeStampDay = date.time
                if (timeStampDay >= startOfWeek) { // if the date is withing a 7-day time frame

                    val number = document.get("totalCoins").toString().toInt()
                    coinCounter += number
                    totalTime += document.get("duration").toString().toInt()

                }
            }

            totalTime = totalTime / coinCounter // we want the average duration
            var totalCoins : IntArray = intArrayOf(coinCounter, totalTime)


        }
    }

    companion object {
        fun newInstance(): Scoreboard = Scoreboard()
    }
}

