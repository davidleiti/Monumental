package ubb.license.david.foursquareapitesting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import io.reactivex.android.schedulers.AndroidSchedulers

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var networkAdapter: NetworkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        for (label in categoryLabels()) {
            val cb = CheckBox(this)
            cb.text = label
            cb.isSelected = false
            layout_categories.addView(cb)
        }

        val values = arrayOf("Cluj-Napoca", "Satu Mare", "Linz")
        val adapter = ArrayAdapter<String>(this, R.layout.simple_spinner_item, R.id.text_location, values)
        spinner_location.adapter = adapter

        button_request.setOnClickListener(this)

        networkAdapter = NetworkAdapter.instance
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_request -> {
                val location = getLocation()
                val radius = getRadius()
                val categories = getCategories()
                networkAdapter.fetchAll(location, radius, categories)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("MainLogger", "Received ResponseBody=$it")
                        it.string()
                    }, {
                        Log.d("MainLogger", "Error:${it.message}")
                    }, {
                        Log.d("MainLogger", "Completed")
                    })
            }
        }
    }

    private fun getCategories(): String {
        var acc = ""
        for (i in 0 until layout_categories.childCount) {
            val cb = layout_categories.getChildAt(i) as CheckBox
            if (cb.isChecked) {
                acc += "${getCategoryId(cb.text.toString())},"
            }
        }
        return if (acc.isNotEmpty()) acc.substring(0, acc.length - 1) else ""
    }

    private fun getLocation(): String {
        return when (spinner_location.selectedItemPosition) {
            0 -> LOC_CLUJ
            1 -> LOC_SM
            2 -> LOC_LINZ
            else -> LOC_CLUJ
        }
    }

    private fun getRadius(): Int {
        return when (sb_radius.progress) {
            0 -> SELECTION_RADIUS_0
            1 -> SELECTION_RADIUS_1
            2 -> SELECTION_RADIUS_2
            3 -> SELECTION_RADIUS_3
            else -> SELECTION_RADIUS_0
        }
    }
    
    private fun getCategoryId(category: String): String {
        return when (category) {
            "Historic Site" -> CATEGORY_HISTORIC_SITE
            "Memorial Site" -> CATEGORY_MEMORIAL_SITE
            "Museum" -> CATEGORY_MUSEUM
			"Opera House" -> CATEGORY_OPERA_HOUSE
			"Theater" -> CATEGORY_THEATER
			"Public Art" -> CATEGORY_PUBLIC_ART
			"Outdoor Sculpture" -> CATEGORY_OUTDOOR_SCULPTURE
            "Street Art" -> CATEGORY_STREET_ART
			"Stadium" -> CATEGORY_STADIUM
			"Lighthouse" -> CATEGORY_LIGHTHOUSE
			"Bridge" -> CATEGORY_BRIDGE
			"Castle" -> CATEGORY_CASTLE
			"Cultural Center" -> CATEGORY_CULTURAL_CENTER
			"Monument" -> CATEGORY_MONUMENT
			"City Hall" -> CATEGORY_CITY_HALL
            else -> ""
        }
    }

    private fun categoryLabels(): List<String> {
        return listOf(
            "Historic Site", "Memorial Site", "Museum", "Opera House", "Theater", "Public Art", "Outdoor Sculpture",
            "Street Art", "Stadium", "Lighthouse", "Bridge", "Castle", "Cultural Center", "Monument", "City Hall"
        )
    }

    companion object {
        const val SELECTION_RADIUS_0 = 0
        const val SELECTION_RADIUS_1 = 1000
        const val SELECTION_RADIUS_2 = 50000
        const val SELECTION_RADIUS_3 = 100000

        const val CATEGORY_HISTORIC_SITE = "4deefb944765f83613cdba6e"
        const val CATEGORY_MEMORIAL_SITE = "5642206c498e4bfca532186c"
        const val CATEGORY_MUSEUM = "4bf58dd8d48988d181941735"
        const val CATEGORY_OPERA_HOUSE = "4bf58dd8d48988d136941735"
        const val CATEGORY_THEATER = "4bf58dd8d48988d137941735"
        const val CATEGORY_PUBLIC_ART = "507c8c4091d498d9fc8c67a9"
        const val CATEGORY_OUTDOOR_SCULPTURE = "52e81612bcbc57f1066b79ed"
        const val CATEGORY_STREET_ART = "52e81612bcbc57f1066b79ee"
        const val CATEGORY_STADIUM = "4bf58dd8d48988d184941735"
        const val CATEGORY_LIGHTHOUSE = "4bf58dd8d48988d15d941735"
        const val CATEGORY_BRIDGE = "4bf58dd8d48988d1df941735"
        const val CATEGORY_CASTLE = "50aaa49e4b90af0d42d5de11"
        const val CATEGORY_CULTURAL_CENTER = "52e81612bcbc57f1066b7a32"
        const val CATEGORY_MONUMENT = "4bf58dd8d48988d12d941735"
        const val CATEGORY_CITY_HALL = "4bf58dd8d48988d129941735"

        const val LOC_CLUJ = "46.7709,23.5899"
        const val LOC_SM = "47.7933,22.8770"
        const val LOC_LINZ = "48.3069,14.2858"
    }

}
