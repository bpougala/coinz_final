package biko.pougala.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.util.Log
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_bank.*
import java.time.LocalDate


class BankFragment: Fragment() {

    private var headline : TextView? = null
    private var coinCount = 0
    private var firestore: FirebaseFirestore? = null
    private var firestoreCoins : CollectionReference? = null
    private var graphWeekly : GraphView? = null
    private var tag_new = "BankFragment"
    private lateinit var  values: MutableList<DataPoint>
    private lateinit var dates: MutableList<Date>
    private lateinit var times: MutableList<DataPoint>
    private var timesWeekly : GraphView? = null // used to store the durations for each coin hunt

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val username = coins.username
        val view = inflater.inflate(R.layout.fragment_bank, container, false)
        headline = view.findViewById(R.id.total_weekly)
        val coinCounter = coins.coinCounter
        headline?.text = getString(R.string.coinsBank, coinCounter)
        values = mutableListOf()
        dates = mutableListOf()
        times = mutableListOf()

        graphWeekly = view.findViewById(R.id.graphWeekly)
        timesWeekly = view.findViewById(R.id.graphWeeklyTime)



        firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()

        firestore?.firestoreSettings = settings

        // We'll get the start of the current week
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startOfWeek = cal.timeInMillis
        val startAsDate = cal.time.toString()

        var i = 1
        // Iterate over all documents in the bank
        Log.d(tag_new, "The username is $username")
        val coinsRef  = firestore?.collection("users-bank")?.document(username)?.collection("coins")

        coinsRef?.get()?.addOnSuccessListener {
            result ->
            for (document in result) {
                // In order to only get dates within a specific timeframe (starting with the current week-, we convert all dates to timestamps
                // and do simple numeric comparisons

                val id = document.id
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                val today = formatter.parse(LocalDate.now().toString()).time
                val date = formatter.parse(id)
                val timeStampDay = date.time
                if (timeStampDay >= startOfWeek) { // if the date is withing a 7-day time frame
                    dates.add(date)
                    i++
                    val number = document.get("totalCoins").toString().toInt()
                    val duration = document.get("duration").toString().toDouble()
                    val timePoint = DataPoint(date, duration)
                    times.add(timePoint)
                    coinCount += number

                    val point = DataPoint(date, number.toDouble())
                    values.add(point)

                }

            }

            // set date label formatter
            Log.d(tag_new, values.toString())

            val seriesGraph: LineGraphSeries<DataPoint> = LineGraphSeries(values.toTypedArray())
            val timesGraph: BarGraphSeries<DataPoint> = BarGraphSeries(times.toTypedArray())
            timesWeekly?.addSeries(timesGraph)
            graphWeekly?.addSeries(seriesGraph)

            graphWeekly?.gridLabelRenderer?.labelFormatter = DateAsXAxisLabelFormatter(activity)
            timesWeekly?.gridLabelRenderer?.labelFormatter = DateAsXAxisLabelFormatter(activity)

            graphWeekly?.gridLabelRenderer?.numHorizontalLabels = values.size
            timesWeekly?.gridLabelRenderer?.numHorizontalLabels = times.size


            val minX = values[1].x
            val maxX = values.last().x


            graphWeekly?.viewport?.setMinX(minX)
            graphWeekly?.viewport?.setMaxX(maxX)


            graphWeekly?.viewport?.isXAxisBoundsManual = true

            // Now we can display the total number of coins in the hea
            }


        return view

    }
    companion object {
        fun newInstance(username: String): BankFragment {
            val fragment = BankFragment()
            val args = Bundle()
            args.putString("username", username)
            fragment.arguments = args
            return fragment
        }
    }
}

