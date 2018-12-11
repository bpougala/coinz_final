// This activity fulfills the bank requirement from the coursework.

package biko.pougala.coinz

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_bank_tabbed.*
import kotlinx.android.synthetic.main.fragment_bank_tabbed.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class BankActivity : AppCompatActivity() {

    private var coinCount = 0
    private var firestore: FirebaseFirestore? = null
    // private var firestoreCoins : CollectionReference? = null
    private var username = ""
    private val tag = "BankActivity"
    private var headline: TextView? = null

    private var graphWeekly: GraphView? = null
    private lateinit var values: MutableList<DataPoint> // this List will store the DataPoints to construct the graph
    private lateinit var dates: MutableList<Date> // this Array will store the dates to build the graph that will later be sorted


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)
        //coinCount = intent.getIntExtra("coinCounter", 0)
        username = intent.getStringExtra("username")

        initToolbar()
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val adapter = PagerManager(supportFragmentManager, username)

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.bank)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.plane)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.score)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {

            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }
        })


    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Bank"



    }

    override fun onStart() {
        super.onStart()
    }
    //TODO: Put graphs for in-depth statistics about daily and weekly coins



}
