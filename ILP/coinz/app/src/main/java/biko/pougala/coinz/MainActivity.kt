package biko.pougala.coinz

import android.app.Application
import android.content.Intent
import android.location.Location
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.*
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdate
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import org.json.JSONObject
import java.time.LocalDate
import java.util.*
import biko.pougala.coinz.R
import timber.log.Timber
import java.lang.Double.sum

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
}
class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener, DownloadCompleteListener {


    private val tag = "MainActivity"
    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private var ACCESS_TOKEN =
        "pk.eyJ1IjoiYnBvdWdhbGEiLCJhIjoiY2pqaGE1empjNTE1ZzN3cjVnZ2RnN3RoNSJ9.EYB-fQasYyuPp9hPeSE_FA"
    private var commenceButton: Button? = null
    private var locations = HashMap<Location, ArrayList<String>>()
    private var isChasing: Boolean = false
    private var rates = HashMap<String, Double>()
    private var username = ""
    private var coinCounter = 0 // this will be used to count how many coins were collected by the user
    private var coinClock: TextView? = null
    private var bankButton: Button? = null

    private var firestore: FirebaseFirestore? = null
    private var firestoreCoins: DocumentReference? = null


    private lateinit var originLocation: Location
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationLayerPlugin: LocationLayerPlugin


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val coins = coins
        username = intent.getStringExtra("username")

        val text = "Hi, ${username}!"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
        val coinCountText = getString(R.string.coinValue, coinCounter)
        coinClock = findViewById(R.id.coinCounter)
        coinClock?.text = coinCountText

        Mapbox.getInstance(this, ACCESS_TOKEN)

        commenceButton = findViewById(R.id.startChasingButton)
        mapView = findViewById(R.id.mapboxMapView)

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

       // bankButton = findViewById(R.id.)

      //  bankButton = findViewById(R.id.bankButton)
       /** val actionBar = supportActionBar
        actionBar?.setDisplayShowHomeEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setCustomView(bankButton)**/

        // People could cheat and go well above the 25-coin per day limit by just restarting the app. Therefore, the starting point
        // should not always be zero but the number of coins collected that day ; if no coin has been collected, then it will
        // be zero. Otherwise, start from that number.


        commenceButton?.setOnClickListener {
            changeButton(commenceButton, false)
            val position = CameraPosition.Builder()
                .zoom(18.0)
                .build()
            map?.cameraPosition = position
            isChasing = true
        }


        firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        firestore?.firestoreSettings = settings

        val today = LocalDate.now().toString()
        val coinsRef = firestore?.collection("users-bank")?.document(username)?.collection("coins")
            ?.document(today)

        coinsRef?.get()?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val coins = task.result?.data?.count()
                if(coins != null) {
                    coinCounter = coins.toInt() -1

                } else {
                    coinCounter = 0
                }
                coinClock?.text = getString(R.string.coinValue, coinCounter)

            }
        }

        coins.coinCounter = coinCounter

        Log.d(tag, "View is done loading")


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.coinhunt_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.bank_action -> {
                openBankAccount()
                true
            } else -> {
                return super.onOptionsItemSelected(item)
            }
        }

    }

    fun openBankAccount() {
        // start a BankActivity
        val intent = Intent(this@MainActivity, BankActivity::class.java)
        intent.putExtra("coinCounter", coinCounter)
        intent.putExtra("username", username)

        startActivity(intent)
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapboxMap is null")
        } else {
            Log.d(tag, "Just about to load Map")
            map = mapboxMap
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true
            val position = CameraPosition.Builder()
                .zoom(14.0)
                .build()
            // make location information available
            map?.cameraPosition = position
            enableLocation()

            // Download the current day's map
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH) + 1 // Months are indexed on 0 not 1
            val day = currentDate.get(Calendar.DAY_OF_MONTH)
            var monthCorrect = month.toString()
            var dayCorrect = day.toString()

            // the coins maps database has a leading zero for month and day. We thus need to take it into account
            Log.d(tag, "Current month is $month")
            if(month+1 < 10) {
                val monthString = month.toString()
                monthCorrect = "0$monthString"
            }

            if (day < 10) {
                val dayString = day.toString()
                dayCorrect = "0$dayCorrect"
            }
            val address = "https://homepages.inf.ed.ac.uk/stg/coinz/$year/$monthCorrect/$dayCorrect/coinzmap.geojson"
            Log.d(tag, "The address is $address")


            DownloadFileTask(this@MainActivity).execute(address)

            Log.d(tag, "Map is done downloading")

        }
    }


    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initialiseLocationEngine()
            initialiseLocationLayer()
        } else {
            Log.d(tag, "Permissions are not granted")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }


    private fun changeButton(button: Button?, value: Boolean?) {

        // The variable "value" determines whether we want the button to appear or disappear
        // This function is made to counteract the fact that we can't change the visibility of the button directly in the
        // setOnClickListener callback on line 81.
        if (value == true) {
            button?.visibility = View.VISIBLE
        } else {
            button?.visibility = View.GONE
        }
    }
    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationEngine() {
        locationEngine = LocationEngineProvider(this)
            .obtainBestLocationEngineAvailable()
        locationEngine.addLocationEngineListener(this)
        locationEngine.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else {
            locationEngine.addLocationEngineListener(this)
        }
    }

    private fun initialiseLocationLayer() {
        if (mapView == null) {
            Log.d(tag, "mapView is null")
        } else {
            if (map == null) {
                Log.d(tag, "map is null")
            } else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                locationLayerPlugin.apply {
                    setLocationLayerEnabled(true)
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }

    private fun setCameraPosition(location: Location) {
        val latlng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
    }

    override fun onLocationChanged(location: Location?) {

        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            originLocation = location
            setCameraPosition(originLocation)

            if(isChasing == true) {
                startChasing(location)
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d(tag, "Permissions: $permissionsToExplain")
        // TODO: Present popup message or dialog
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted) {
            enableLocation()
        } else {
            // TODO: Open a dialogue with the user
        }
    }

    public override fun onStart() {
        super.onStart()
        mapView?.onStart()

    }

    override fun downloadComplete(result: String) {
        val fs = FeatureCollection.fromJson(result)
        val features = fs.features()

        /** We retrieve the values iN GOLD of each currency and store them in a HashMap to use them later to convert each
         collected coin to its equivalent in GOLD
        **/

        val features_js = JSONObject(result)
        val rates_js = features_js.getJSONObject("rates")
        val value_shil = rates_js.getString("SHIL")
        rates.put("SHIL", value_shil.toDouble())

        val value_dolr = rates_js.getString("DOLR")
        rates.put("DOLR", value_dolr.toDouble())

        val value_quid = rates_js.getString("QUID")
        rates.put("QUID", value_quid.toDouble())

        val value_peny = rates_js.getString("PENY")
        rates.put("PENY", value_peny.toDouble())



        for (f in features.orEmpty()) { // this list is of nullable type, we have to make sure only non-null arrays get called here
            val g = f.geometry() as Point
            val coordinates = g.coordinates()
            val j = f.properties()
            val value = j?.get("value").toString()
            val money = value + " " + j?.get("currency").toString()
            val location = Location("")
            location.latitude = coordinates[1]
            location.longitude = coordinates[0]
            map?.addMarker(MarkerOptions().title(j?.get("currency").toString()).snippet(value).position(LatLng(coordinates[1], coordinates[0])))
            val content = ArrayList<String>()
            content.add(money)
            content.add(j?.get("id").toString())
            locations.put(location, content) // we hold the value of a coin and its location in a hashmap to use it later during coins-hunting
        }

    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView?.onStop()
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
    }


    private fun startChasing(location: Location) {

        val factory = LayoutInflater.from(this@MainActivity)
        val view = factory.inflate(R.layout.collect_new_coin, null)
        val image: ImageView = view.findViewById(R.id.coin_icon)
        val coinDescription: TextView = view.findViewById(R.id.collect_text)
        image.setImageResource(R.drawable.coin)

        // whenever a new location is available, this function will compute the distance between the user and each coin.
        // if the distance is less than 500 meters, the user will have the possibility to collect it.
        if(coinCounter < 26) {
            for ((loc, content) in locations) {

                val distance = computeDistance(location, loc)
                if (distance <= 0.25) { // the distance is in kilometers
                    val builder = AlertDialog.Builder(this@MainActivity)
                    val collectCoinText = getString(R.string.foundCoin, content.get(0))
                    coinDescription.text = collectCoinText
                    builder.setView(view)
                    // Link the AlertDialog to the new_coin_alert.xml layout file
                    builder.setTitle("New Coin available!")
                    //   builder.setMessage()
                    builder.setPositiveButton(R.string.collect) { dialog, which ->
                        dialog.dismiss()
                        coinCounter++
                        coins.coinCounter = coinCounter
                        val coinCountText = getString(R.string.coinValue, coinCounter)
                        coinClock?.text = coinCountText
                        val coin = convertToGOLD(content.get(0))
                        val today = LocalDate.now().toString()
                        val newCoin = mapOf(
                            "gold_${coinCounter}" to coin,
                            //    "username" to username
                                 "totalCoins" to coinCounter
                        )

                        firestoreCoins = firestore?.collection("users-bank")?.document(username)?.collection("coins")
                            ?.document(today)
                        firestoreCoins?.set(newCoin, SetOptions.merge())
                            ?.addOnSuccessListener {
                                val toast = Toast.makeText(applicationContext, "Coin collected", Toast.LENGTH_SHORT)
                                toast.show()
                            }
                            ?.addOnFailureListener { e -> Log.e(tag, e.message) }
                    }
                    builder.setNegativeButton("Discard") { dialog, which ->
                        dialog.dismiss()
                        locations.remove(loc) // if the user discards the coin, it won't be proposed again
                    }
                    builder.show()


                    break

                }
            }
        } else {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("You reached your daily limit")
            builder.setMessage("Only 25 coins can be collected on a daily basis. Please try again tomorrow.")
            builder.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()

            }
            builder.show()
        }
    }

    private fun computeDistance(loc1: Location?, loc2: Location?): Double {
        // It will compute the Euclidean distance between two lat/lng pairs
        // From stackoverflow
        val radius = 6371 // Earth radius
        val lat1 = loc1!!.latitude
        val lat2 = loc2!!.latitude
        val lon1 = loc1.longitude
        val lon2 = loc2.longitude

        val dLat = deg2rad(lat2-lat1)
        val dLon = deg2rad(lon2-lon1)

        val a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val d = radius * c

        return d
    }

    private fun convertToGOLD(value: String): Double {
        var value_ = value.drop(1)
        Log.d(tag, value)
        if ("SHIL" in value) {
            val cur_ = value_.dropLast(8) // remove the currency from the String and convert to Double (ex: "7.3783873 SHIL" -> "7.3783873"
            val cur = cur_.toDouble()
            val shil = rates.getValue("SHIL").toDouble()
            return (cur * shil)

        } else if("DOLR" in value) {
            val cur_ = value_.dropLast(8)
            val cur = cur_.toDouble()
            val dolr = rates.getValue("DOLR").toDouble()
            return (cur * dolr)

        } else if("QUID" in value) {
            val cur_ = value_.dropLast(8)
            val cur = cur_.toDouble()
            val quid = rates.getValue("QUID").toDouble()
            return (cur * quid)

        } else { // edge case being PENY
            val cur_ = value_.dropLast(8)
            val cur = cur_.toDouble()
            val peny = rates.getValue("PENY").toDouble()
            return cur * peny
        }
    }
    fun deg2rad(deg: Double): Double {
        // from StakOverFlow
        return deg * (Math.PI/180)
    }
    class DownloadFileTask(private val caller : DownloadCompleteListener) : AsyncTask<String, Void, String>() {



        private val tag: String = "DownloadFileTask"
        override fun doInBackground(vararg urls: String): String = try {
            loadFileFromNetwork(urls[0])

        } catch (e: IOException) { "Unable to load content. Check your network connection" }

        private fun loadFileFromNetwork(urlString: String): String {
            val stream: InputStream = downloadUrl(urlString)


            //TODO: convert this input stream to a string and then return it
            val result = stream.reader().readText()
            return result
        }

        @Throws(IOException::class)
        private fun downloadUrl(urlString: String): InputStream {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpsURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"

            conn.doInput = true

            conn.connect()
            return conn.inputStream
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            caller.downloadComplete(result)
        }
    }


}

class coins: Application() {

    // this class extending android.app.Application is used to have access to the value of coinCounter across all activities
    // regardless of the Android lifecycle status

    companion object {
        var coinCounter = 0
        var username = ""
    }


}
